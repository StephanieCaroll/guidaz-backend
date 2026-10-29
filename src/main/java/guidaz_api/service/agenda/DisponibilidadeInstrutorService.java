
package guidaz_api.service.agenda;

import guidaz_api.model.agenda.BloqueioAgenda;
import guidaz_api.model.agenda.DisponibilidadeInstrutor;
import guidaz_api.model.usuario.Instrutor;
import guidaz_api.repository.agenda.BloqueioAgendaRepository;
import guidaz_api.repository.agenda.DisponibilidadeInstrutorRepository;
import guidaz_api.repository.usuario.InstrutorRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DisponibilidadeInstrutorService {

    private static final int DURACAO_AULA_MINUTOS = 50;

    private final DisponibilidadeInstrutorRepository repository;
    private final InstrutorRepository instrutorRepository;
    private final BloqueioAgendaRepository bloqueioAgendaRepository;

    public DisponibilidadeInstrutorService(
            DisponibilidadeInstrutorRepository repository,
            InstrutorRepository instrutorRepository,
            BloqueioAgendaRepository bloqueioAgendaRepository
    ) {
        this.repository = repository;
        this.instrutorRepository = instrutorRepository;
        this.bloqueioAgendaRepository = bloqueioAgendaRepository;
    }

    // Consultar os horários ativos de um instrutor.
    public List<DisponibilidadeInstrutor> buscarPorInstrutor(
            Long instrutorId
    ) {
        return repository.findByInstrutorIdAndAtivoTrue(instrutorId);
    }

    // Consultar os horários ativos de um instrutor em um dia da semana.
    public List<DisponibilidadeInstrutor> buscarPorInstrutorEDia(
            Long instrutorId,
            DayOfWeek diaSemana
    ) {
        return repository.findByInstrutorIdAndDiaSemanaAndAtivoTrue(
                instrutorId,
                diaSemana
        );
    }

    // Cadastrar um período de disponibilidade.
    public DisponibilidadeInstrutor criarDisponibilidade(
            Long instrutorId,
            DayOfWeek diaSemana,
            LocalTime horaInicio,
            LocalTime horaFim
    ) {
        if (instrutorId == null || diaSemana == null
                || horaInicio == null || horaFim == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Instrutor, dia da semana e horários são obrigatórios."
            );
        }

        if (!horaFim.isAfter(horaInicio)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O horário final deve ser posterior ao inicial."
            );
        }

        Instrutor instrutor = instrutorRepository.findById(instrutorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Instrutor não encontrado."
                ));

        DisponibilidadeInstrutor disponibilidade =
                new DisponibilidadeInstrutor();

        disponibilidade.setInstrutor(instrutor);
        disponibilidade.setDiaSemana(diaSemana);
        disponibilidade.setHoraInicio(horaInicio);
        disponibilidade.setHoraFim(horaFim);
        disponibilidade.setAtivo(true);

        return repository.save(disponibilidade);
    }

    // Consultar os horários livres de um instrutor em uma data.
    public List<LocalTime> buscarHorariosLivres(
            Long instrutorId,
            LocalDate data
    ) {
        if (instrutorId == null || data == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O instrutor e a data são obrigatórios."
            );
        }

        // Confirma se o instrutor existe.
        instrutorRepository.findById(instrutorId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Instrutor não encontrado."
                ));

        // Descobre o dia da semana da data informada.
        DayOfWeek diaSemana = data.getDayOfWeek();

        // Busca os períodos em que o instrutor trabalha nesse dia.
        List<DisponibilidadeInstrutor> disponibilidades =
                repository.findByInstrutorIdAndDiaSemanaAndAtivoTrue(
                        instrutorId,
                        diaSemana
                );

        // Busca os bloqueios cadastrados para essa data.
        List<BloqueioAgenda> bloqueios =
                bloqueioAgendaRepository
                        .findByInstrutorIdAndData(instrutorId, data);

        List<LocalTime> horariosLivres = new ArrayList<>();

        // Percorre cada período de disponibilidade.
        for (DisponibilidadeInstrutor disponibilidade : disponibilidades) {

            LocalTime inicio = disponibilidade.getHoraInicio();
            LocalTime fim = disponibilidade.getHoraFim();

            // Gera aulas de 50 minutos, sem ultrapassar o fim do período.
            LocalTime horario = inicio;

            while (!horario.plusMinutes(DURACAO_AULA_MINUTOS).isAfter(fim)) {

                LocalTime fimAula =
                        horario.plusMinutes(DURACAO_AULA_MINUTOS);

                boolean bloqueado = false;

                // Verifica se a aula coincide com algum bloqueio.
                for (BloqueioAgenda bloqueio : bloqueios) {

                    // Um bloqueio de dia inteiro elimina todos os horários.
                    if (bloqueio.isDiaInteiro()) {
                        bloqueado = true;
                        break;
                    }

                    LocalTime inicioBloqueio = bloqueio.getHoraInicio();
                    LocalTime fimBloqueio = bloqueio.getHoraFim();

                    // Verifica se os intervalos se sobrepõem.
                    boolean existeSobreposicao =
                            horario.isBefore(fimBloqueio)
                            && fimAula.isAfter(inicioBloqueio);

                    if (existeSobreposicao) {
                        bloqueado = true;
                        break;
                    }
                }

                if (!bloqueado) {
                    horariosLivres.add(horario);
                }

                // O próximo horário começa após a aula de 50 minutos.
                horario = fimAula;
            }
        }

        return horariosLivres.stream()
                .distinct()
                .sorted()
                .toList();
    }
}
