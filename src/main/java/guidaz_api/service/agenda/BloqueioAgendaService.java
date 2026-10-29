
package guidaz_api.service.agenda;

import guidaz_api.model.agenda.BloqueioAgenda;
import guidaz_api.model.usuario.Instrutor;
import guidaz_api.repository.agenda.BloqueioAgendaRepository;
import guidaz_api.repository.usuario.InstrutorRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class BloqueioAgendaService {

    private final BloqueioAgendaRepository repository;
    private final InstrutorRepository instrutorRepository;

    public BloqueioAgendaService(
            BloqueioAgendaRepository repository,
            InstrutorRepository instrutorRepository
    ) {
        this.repository = repository;
        this.instrutorRepository = instrutorRepository;
    }

    // Consultar os bloqueios de um instrutor em uma data
    public List<BloqueioAgenda> buscarPorInstrutorEData(
            Long instrutorId,
            LocalDate data
    ) {
        return repository.findByInstrutorIdAndData(
                instrutorId,
                data
        );
    }

    // Cadastrar um bloqueio na agenda
    public BloqueioAgenda criarBloqueio(
            Long instrutorId,
            LocalDate data,
            LocalTime horaInicio,
            LocalTime horaFim,
            boolean diaInteiro,
            String motivo
    ) {
        if (instrutorId == null || data == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O instrutor e a data são obrigatórios."
            );
        }

        // Verificar se o instrutor existe
        Instrutor instrutor = instrutorRepository.findById(instrutorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Instrutor não encontrado."
                ));

        // Validar os horários de um bloqueio parcial
        if (!diaInteiro) {
            if (horaInicio == null || horaFim == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Informe o horário inicial e o horário final."
                );
            }

            if (!horaFim.isAfter(horaInicio)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O horário final deve ser posterior ao horário inicial."
                );
            }
        }

        BloqueioAgenda bloqueio = new BloqueioAgenda();
        bloqueio.setInstrutor(instrutor);
        bloqueio.setData(data);
        bloqueio.setDiaInteiro(diaInteiro);
        bloqueio.setMotivo(motivo);

        if (diaInteiro) {
            bloqueio.setHoraInicio(null);
            bloqueio.setHoraFim(null);
        } else {
            bloqueio.setHoraInicio(horaInicio);
            bloqueio.setHoraFim(horaFim);
        }

        return repository.save(bloqueio);
    }
}
