package com.example.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class ServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}

}


@Entity
class Beer {

	@Id
	@GeneratedValue
	private Long id;
	private String name;

	public Beer() {
	}

	public Beer(String name){
	    this.name = name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
	    return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Beer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}

@RepositoryRestResource
interface BeerRepository extends JpaRepository<Beer, Long> {

}

@Component
class BeerCommandLineRunner implements CommandLineRunner {

    private final BeerRepository repository;

    public BeerCommandLineRunner(BeerRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {

        Stream.of("Corona", "Taquinha", "heineken", "Pacenha").forEach(name -> repository.save(new Beer(name)));
        repository.findAll().forEach(System.out::println);
    }
}

@CrossOrigin(origins = "http://localhost:3000")
@RestController
class BeerController {

    private BeerRepository repository;

    public BeerController(BeerRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/good-beers")
    public Collection<Beer> goodBeers() {

        return repository.findAll().stream().filter(this::isGreat).collect(Collectors.toList());
    }

    @GetMapping("/beers")
    public Collection<Beer> beers() {

        return repository.findAll().stream().collect(Collectors.toList());
    }

    @PostMapping("/add-beer")
    public void addBeers(@RequestBody String name) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> map = new HashMap<String, String>();
            map = mapper.readValue(name, new TypeReference<Map<String, String>>() {});
            repository.save(new Beer(map.get("name")));
            repository.findAll().forEach(System.out::println);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public boolean isGreat(Beer beer) {
        return !beer.getName().equals("Pacenha") &&
                !beer.getName().equals("Taquinha");
    }
}