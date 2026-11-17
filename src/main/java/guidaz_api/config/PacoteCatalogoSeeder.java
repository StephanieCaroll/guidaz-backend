package guidaz_api.config;

import guidaz_api.model.pedido.PacoteCatalogo;
import guidaz_api.repository.pedido.PacoteCatalogoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class PacoteCatalogoSeeder implements CommandLineRunner {

    private final PacoteCatalogoRepository pacoteRepository;

    public PacoteCatalogoSeeder(PacoteCatalogoRepository pacoteRepository) {
        this.pacoteRepository = pacoteRepository;
    }

    @Override
    public void run(String... args) {
        if (pacoteRepository.countByInstrutorIdIsNull() == 0) {
            pacoteRepository.saveAll(List.of(
                new PacoteCatalogo("A / Moto", "A", 2, new BigDecimal("220.00"), null),
                new PacoteCatalogo("B / Carro", "B", 2, new BigDecimal("280.00"), null),
                new PacoteCatalogo("AB / Moto e Carro", "AB", 4, new BigDecimal("500.00"), null),
                new PacoteCatalogo("C / Caminhão", "C", 10, new BigDecimal("1500.00"), null),
                new PacoteCatalogo("D / Ônibus", "D", 10, new BigDecimal("1400.00"), null),
                new PacoteCatalogo("E / Carreta", "E", 15, new BigDecimal("2510.00"), null)
            ));
        }
    }
}