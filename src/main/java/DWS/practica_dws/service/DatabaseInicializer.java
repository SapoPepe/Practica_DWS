package DWS.practica_dws.service;

import DWS.practica_dws.model.Person;
import DWS.practica_dws.repository.PersonRepository;
import DWS.practica_dws.security.SecurityConfiguration;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatabaseInicializer {
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private SecurityConfiguration securityConfiguration;

    @PostConstruct
    public void init(){
        personRepository.save(new Person("a", securityConfiguration.passwordEncoder().encode("a"), "USER"));
        personRepository.save(new Person("lucia", securityConfiguration.passwordEncoder().encode("adminpass"), "ADMIN"));//"USER",


    }
}
