package br.com.alura.forum.controller;

import java.net.URI;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.alura.forum.controller.dto.DetalhesTopicoDTO;
import br.com.alura.forum.controller.dto.TopicoDTO;
import br.com.alura.forum.controller.form.TopicoForm;
import br.com.alura.forum.controller.form.UpdatedTopicoForm;
import br.com.alura.forum.modelo.Topico;
import br.com.alura.forum.repository.CursoRepository;
import br.com.alura.forum.repository.TopicoRepository;

@RestController
@RequestMapping("/topicos")
public class TopicosController {
	
	@Autowired
	private TopicoRepository topicoRepository;
	@Autowired
	private CursoRepository cursoRepository;

    @GetMapping
    @Cacheable(value = "listaDeTopicos")
    public Page<TopicoDTO> findAll(@RequestParam(required = false) String nomeCurso,
    		@PageableDefault(sort="id", direction=Direction.DESC, page=0, size=10) Pageable paginacao) {
    	
    	if(nomeCurso == null) {
    		Page<Topico> topicos = topicoRepository.findAll(paginacao);
    		return TopicoDTO.converter(topicos);
    	}	
    	else {
    		Page<Topico> topicos = topicoRepository.findByCurso_Nome(nomeCurso, paginacao);
    		return TopicoDTO.converter(topicos);
    	}
    }
    
    @PostMapping
    @Transactional
    @CacheEvict(value = "listaDeTopicos", allEntries = true)
    public ResponseEntity<TopicoDTO> create(@RequestBody @Valid TopicoForm form,
    		UriComponentsBuilder uriBuilder) {
    	
    	Topico topico = form.convert(cursoRepository);
    	topicoRepository.save(topico);
    	
    	URI uri = uriBuilder.path("/topicos/{id}").buildAndExpand(topico.getId()).toUri();
    	return ResponseEntity.created(uri).body(new TopicoDTO(topico));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DetalhesTopicoDTO> detail(@PathVariable Long id) {
    	Optional<Topico> topico = topicoRepository.findById(id);
    	
    	if(topico.isPresent())
    		return ResponseEntity.ok(new DetalhesTopicoDTO(topico.get()));
    	else
    		return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @Transactional
    @CacheEvict(value = "listaDeTopicos", allEntries = true)
    public ResponseEntity<TopicoDTO> update(@PathVariable Long id, @RequestBody @Valid UpdatedTopicoForm form) {
    	Optional<Topico> optional = topicoRepository.findById(id);    	
    	
    	if(optional.isPresent()) {
    		Topico topico = form.update(id, topicoRepository);
    		return ResponseEntity.ok(new TopicoDTO(topico));
    	}
    	return ResponseEntity.notFound().build();
    }
    
    @DeleteMapping("/{id}")
    @Transactional
    @CacheEvict(value = "listaDeTopicos", allEntries = true)
    public ResponseEntity<?> remove(@PathVariable Long id) {
    	Optional<Topico> optional = topicoRepository.findById(id);
    	if(optional.isPresent()) {
	    	topicoRepository.deleteById(id);
	    	return ResponseEntity.ok().build();
    	}
    	return ResponseEntity.notFound().build();
    }
}
