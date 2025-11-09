package com.example.payments.config;

import com.example.payments.model.Payment;
import com.example.payments.model.StatusPagamento;
import com.example.payments.repository.PaymentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final PaymentRepository paymentRepository;

    public DataLoader(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public void run(String... args) {
        if (paymentRepository.count() == 0) {
            paymentRepository.save(new Payment(null, 1001, "12345678900", "boleto", null, 150.00, StatusPagamento.PENDENTE, true));
            paymentRepository.save(new Payment(null, 1002, "98765432100", "cartao_credito", "4111111111111111", 320.75, StatusPagamento.PENDENTE, true));
            paymentRepository.save(new Payment(null, 1003, "56789012345", "cartao_debito", "5500000000000004", 89.90, StatusPagamento.PROCESSADO_FALHA, true));
            paymentRepository.save(new Payment(null, 1004, "10293847566", "pix", null, 999.99, StatusPagamento.PROCESSADO_SUCESSO, true));
            System.out.println("💾 Dados iniciais carregados no H2!");
        }
    }
}
