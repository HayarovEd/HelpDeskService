package ru.impuls.helpdeskservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.impuls.helpdeskservice.dto.request.GroupEditDto;
import ru.impuls.helpdeskservice.dto.response.GroupDto;
import ru.impuls.helpdeskservice.service.GroupService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<GroupDto> getAllGroups() {
        return groupService.getAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public GroupDto getById(@PathVariable UUID id) {
        return groupService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupDto create(@RequestBody @Valid GroupEditDto groupDto) {
        return groupService.create(groupDto);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public GroupDto update(@PathVariable UUID id, @RequestBody @Valid GroupEditDto groupDto) {
        return groupService.update(id, groupDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public GroupDto delete(@PathVariable UUID id) {
        return groupService.delete(id);
    }
}