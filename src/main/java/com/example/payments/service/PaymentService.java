package com.example.payments.service;

import com.example.payments.model.Payment;
import com.example.payments.model.StatusPagamento;
import com.example.payments.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository repository;

    public PaymentService(PaymentRepository repository) {
        this.repository = repository;
    }

    // 🔹 1. Listar pagamentos (com ou sem filtros)
    public List<Payment> findAll() {
        return repository.findAll();
    }

    public Optional<Payment> findById(Long id) {
        return repository.findById(id);
    }

    // 🔹 2. Criar pagamento
    public Payment save(Payment payment) {
        payment.setStatus(StatusPagamento.PENDENTE);
        payment.setAtivo(true);
        return repository.save(payment);
    }

    // 🔹 3. Atualizar status com regras de negócio
    public Payment atualizarStatus(Long id, StatusPagamento novoStatus) {
        Payment payment = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

        StatusPagamento atual = payment.getStatus();

        // ⚙️ Regras de transição de status
        switch (atual) {
            case PENDENTE:
                if (novoStatus == StatusPagamento.PROCESSADO_SUCESSO ||
                        novoStatus == StatusPagamento.PROCESSADO_FALHA) {
                    payment.setStatus(novoStatus);
                } else {
                    throw new RuntimeException("Status inválido para pagamento pendente");
                }
                break;

            case PROCESSADO_SUCESSO:
                throw new RuntimeException("Pagamentos processados com sucesso não podem ser alterados");

            case PROCESSADO_FALHA:
                if (novoStatus == StatusPagamento.PENDENTE) {
                    payment.setStatus(StatusPagamento.PENDENTE);
                } else {
                    throw new RuntimeException("Pagamento com falha só pode voltar para PENDENTE");
                }
                break;
        }

        return repository.save(payment);
    }

    // 🔹 4. Exclusão lógica
    public Payment inativarPagamento(Long id) {
        Payment payment = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

        if (payment.getStatus() != StatusPagamento.PENDENTE) {
            throw new RuntimeException("Só é possível inativar pagamentos pendentes");
        }

        payment.setAtivo(false);
        return repository.save(payment);
    }

    // 🔹 Exclusão física (se realmente precisar)
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
