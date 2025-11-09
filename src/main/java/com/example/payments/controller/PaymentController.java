package com.example.payments.controller;

import com.example.payments.model.Payment;
import com.example.payments.model.StatusPagamento;
import com.example.payments.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @PostMapping
    public Payment criarPagamento(@RequestBody Payment payment) {
        payment.setStatus(StatusPagamento.PENDENTE);
        payment.setAtivo(true);
        return paymentRepository.save(payment);
    }

    @GetMapping
    public List<Payment> listarPagamentos(
            @RequestParam(name = "codigoDebito", required = false) Integer codigoDebito,
            @RequestParam(name = "cpfCnpjPagador", required = false) String cpfCnpjPagador,
            @RequestParam(name = "status", required = false) StatusPagamento status
    ) {
        List<Payment> pagamentos = paymentRepository.findAll();

        if (codigoDebito != null) {
            pagamentos = pagamentos.stream()
                    .filter(p -> p.getCodigoDebito().equals(codigoDebito))
                    .toList();
        }

        if (cpfCnpjPagador != null) {
            pagamentos = pagamentos.stream()
                    .filter(p -> p.getCpfCnpjPagador().equals(cpfCnpjPagador))
                    .toList();
        }

        if (status != null) {
            pagamentos = pagamentos.stream()
                    .filter(p -> p.getStatus() == status)
                    .toList();
        }

        return pagamentos;
    }

    @GetMapping("/{id}")
    public Optional<Payment> buscarPorId(@PathVariable("id") Long id) {
        return paymentRepository.findById(id);
    }

    public static class AtualizarStatusRequest {
        private String novoStatus;

        public String getNovoStatus() {
            return novoStatus;
        }

        public void setNovoStatus(String novoStatus) {
            this.novoStatus = novoStatus;
        }
    }

    @PutMapping("/{id}/status")
    public Payment atualizarStatus(@PathVariable("id") Long id, @RequestBody AtualizarStatusRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

        StatusPagamento novoStatus = StatusPagamento.valueOf(request.getNovoStatus().toUpperCase());
        StatusPagamento statusAtual = payment.getStatus();

        switch (statusAtual) {
            case PENDENTE:
                if (novoStatus == StatusPagamento.PROCESSADO_SUCESSO ||
                        novoStatus == StatusPagamento.PROCESSADO_FALHA) {
                    payment.setStatus(novoStatus);
                } else {
                    throw new IllegalArgumentException("Transição inválida a partir de PENDENTE.");
                }
                break;

            case PROCESSADO_SUCESSO:
                throw new IllegalArgumentException("Pagamento já processado com sucesso e não pode ser alterado.");

            case PROCESSADO_FALHA:
                if (novoStatus == StatusPagamento.PENDENTE) {
                    payment.setStatus(novoStatus);
                } else {
                    throw new IllegalArgumentException("Quando processado com falha, só pode voltar para PENDENTE.");
                }
                break;
        }

        return paymentRepository.save(payment);
    }

    @DeleteMapping("/{id}")
    public void deletarPagamento(@PathVariable("id") Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

        if (payment.getStatus() == StatusPagamento.PENDENTE) {
            payment.setAtivo(false);
            paymentRepository.save(payment);
        } else {
            throw new IllegalArgumentException("Somente pagamentos PENDENTES podem ser inativados.");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
    public String handleIllegalArgument(IllegalArgumentException e) {
        return e.getMessage();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
    public String handleRuntime(RuntimeException e) {
        return e.getMessage();
    }
}
