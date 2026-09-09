package com.fitconnect.classservice.api;

import com.fitconnect.classservice.domain.*;
import com.fitconnect.classservice.service.FitnessClassService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/classes")
public class FitnessClassController {
    private final FitnessClassService service;
    public FitnessClassController(FitnessClassService service){this.service=service;}
    @GetMapping public Page<FitnessClassResponse> list(@RequestParam(required=false) ClassCategory category,@RequestParam(required=false) ClassLevel level,@RequestParam(required=false) LocalDateTime dateFrom,@RequestParam(required=false) LocalDateTime dateTo,@RequestParam(required=false) String location,@RequestParam(required=false) String instructor,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="10") int size,@RequestParam(defaultValue="dateTime,asc") String sort){return service.search(category,level,dateFrom,dateTo,location,instructor,page,size,sort);}
    @GetMapping("/search") public Page<FitnessClassResponse> search(@RequestParam(required=false) ClassCategory category,@RequestParam(required=false) ClassLevel level,@RequestParam(required=false) LocalDateTime dateFrom,@RequestParam(required=false) LocalDateTime dateTo,@RequestParam(required=false) String location,@RequestParam(required=false) String instructor,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="10") int size,@RequestParam(defaultValue="dateTime,asc") String sort){return list(category,level,dateFrom,dateTo,location,instructor,page,size,sort);}
    @GetMapping("/{id}") public FitnessClassResponse get(@PathVariable Long id){return service.get(id);}
    @PostMapping public ResponseEntity<FitnessClassResponse> create(@Valid @RequestBody FitnessClassRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));}
    @PutMapping("/{id}") public FitnessClassResponse update(@PathVariable Long id,@Valid @RequestBody FitnessClassRequest request){return service.update(id,request);}
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id){service.cancel(id);return ResponseEntity.noContent().build();}
    @PatchMapping("/{id}/increment") public FitnessClassResponse increment(@PathVariable Long id,@RequestParam int spots){return service.increment(id,spots);}
    @PatchMapping("/{id}/decrement") public FitnessClassResponse decrement(@PathVariable Long id,@RequestParam int spots){return service.decrement(id,spots);}
}
