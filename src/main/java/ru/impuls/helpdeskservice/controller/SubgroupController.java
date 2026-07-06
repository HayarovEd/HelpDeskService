package ru.impuls.helpdeskservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subgroups")
@RequiredArgsConstructor
public class SubgroupController {

    /*private final SubgroupService subgroupService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubgroupResponse create(@RequestParam UUID groupId,
                                   @RequestBody @Valid SubgroupCreateRequest subgroupRequest) {
        return subgroupService.create(groupId, subgroupRequest);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SubgroupResponse update(@PathVariable UUID id,
                                   @RequestBody @Valid SubgroupEditRequest subgroupRequest) {
        return subgroupService.update(id, subgroupRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SubgroupResponse delete(@PathVariable UUID id) {
        return subgroupService.delete(id);
    }*/
}
