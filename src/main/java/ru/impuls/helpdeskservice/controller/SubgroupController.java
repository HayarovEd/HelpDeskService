package ru.impuls.helpdeskservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.impuls.helpdeskservice.dto.SubgroupDto;
import ru.impuls.helpdeskservice.dto.request.SubgroupEditDto;
import ru.impuls.helpdeskservice.service.SubgroupService;

import java.util.UUID;

@RestController
@RequestMapping("/subgroups")
@RequiredArgsConstructor
public class SubgroupController {

    private final SubgroupService subgroupService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubgroupDto create(@RequestParam UUID groupId, @RequestBody @Valid SubgroupEditDto subgroupDto) {
        return subgroupService.create(groupId, subgroupDto);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SubgroupDto update(@PathVariable UUID id, @RequestBody @Valid SubgroupEditDto subgroupDto) {
        return subgroupService.update(id, subgroupDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SubgroupDto delete(@PathVariable UUID id) {
        return subgroupService.delete(id);
    }
}
