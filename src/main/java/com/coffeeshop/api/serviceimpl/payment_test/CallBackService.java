package com.coffeeshop.api.serviceimpl.payment_test;


import com.coffeeshop.api.domain.Payment;
import com.coffeeshop.api.domain.PaymentCallbackEvent;
import com.coffeeshop.api.domain.enums.PaymentStatus;
import com.coffeeshop.api.dto.payment_test.CallBackRequest;
import com.coffeeshop.api.dto.payment_test.CheckTransactionResponse;
import com.coffeeshop.api.repository.payment_test.PaymentCallbackEventRepository;
import com.coffeeshop.api.repository.payment_test.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CallBackService {

    private final CallbackSecurityService callbackSecurityService;
    private final PaymentService paymentService;
    private final PaymentCallbackEventRepository paymentCallbackEventRepository;
    private final PaymentRepository paymentRepository;



    @Async
    @Transactional
    public void handleAsync (String token, CallBackRequest callBackRequest) {

        // 1) Security
        callbackSecurityService.verifyToke(token);

        String tranId = callBackRequest.tranId().trim();
        String status = callBackRequest.status().trim();
        String apv = callBackRequest.apv() == null? "" : callBackRequest.apv().trim();

        // 2) Idempotency (avoid duplicates)
        String dedupeKey = tranId + "|" + status + "|" + apv;
        if(paymentCallbackEventRepository.existsByDedupeKey(dedupeKey)){
            log.info("Duplicate callback ignored dedupeKey={}", dedupeKey);
            return;
        }

        PaymentCallbackEvent event = new PaymentCallbackEvent();
        event.setDedupeKey(dedupeKey);
        event.setTranId(tranId);
        event.setStatus(status);
        event.setApv(apv);
        paymentCallbackEventRepository.save(event);

        // 3) Verify with PayWay Check Transaction (source of truth)
        CheckTransactionResponse verified = paymentService.checkTransaction(tranId);
        if(verified == null || verified.status() == null || !verified.status().code().equals("00")) {
            log.warn("checkTransaction failed for tranId={}, verified={}", tranId, verified);
            return;
        }

        String payStatus = verified.data() != null ? verified.data().paymentStatus() : null;
        log.info("Verified payment_status={} for tranId={}", payStatus, tranId);

        // 4) Update DB
        Payment text = paymentRepository.findByTranId(tranId).orElse(null);
        if(text == null) {
            log.warn("Unknown tranId in DB, ignoring update. tranId={}", tranId);
            return;
        }

        if(payStatus.equals(PaymentStatus.APPROVED.toString())){
            text.markPaid(verified.data().apv());
            paymentRepository.save(text);
            log.info("Transaction marked PAID tranId={}", tranId);
        }
        else {
            // still pending or other status -> keep it pending
            log.info("Transaction not approved yet, keep status. tranId={}, payStatus={}", tranId, payStatus);
        }


    }





}
