package guidaz_api.controller;

import guidaz_api.model.agendamento.Agendamento;
import guidaz_api.repository.AgendamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agendamentos")
@CrossOrigin(origins = "*") // Permite que o front-end Angular acesse a API sem bloqueio de CORS
public class AgendamentoController {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    // Listar todos os agendamentos
    @GetMapping
    public List<Agendamento> listarTodos() {
        return agendamentoRepository.findAll();
    }

    // Cadastrar uma nova reserva de aula (data, hora, categoria, aluno e instrutor)
    @PostMapping("/cadastrar")
    public ResponseEntity<Agendamento> cadastrar(@RequestBody Agendamento agendamento) {
        Agendamento novoAgendamento = agendamentoRepository.save(agendamento);
        return ResponseEntity.ok(novoAgendamento);
    }
}