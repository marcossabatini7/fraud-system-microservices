package com.trazaqui.customer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@AllArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final RestTemplate restTemplate;

    public void registerCustomer(CustomerRegistrationRequest customerRegistrationRequest) {
        Customer customer = Customer.builder()
                .name(customerRegistrationRequest.name())
                .email(customerRegistrationRequest.email())
                .build();

        //  todo: check if email is valid
        //  todo: check if email is not taken
        Customer customerByEmail = customerRepository.findByEmail(customer.getEmail());

        if (customerByEmail == null) {
            customerRepository.saveAndFlush(customer);
        }

        //  todo: check if fraudster
        FraudCheckResponse fraudCheckResponse = restTemplate.getForObject(
                "http://localhost:8081/api/v1/fraud-check/{customerId}",
                FraudCheckResponse.class,
                customer != null && customer.getId() != null
                        ? customer.getId()
                        : customerByEmail.getId()
        );

        if (fraudCheckResponse.isFraudster()) {
            throw new IllegalStateException("fraudster");
        }

        //  todo: send notification
    }
}
