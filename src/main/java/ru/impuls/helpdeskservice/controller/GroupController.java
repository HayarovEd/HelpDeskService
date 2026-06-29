package ru.impuls.helpdeskservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.impuls.helpdeskservice.dto.request.GroupCreateRequest;
import ru.impuls.helpdeskservice.dto.request.GroupEditRequest;
import ru.impuls.helpdeskservice.dto.response.GroupResponse;
import ru.impuls.helpdeskservice.service.GroupService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<GroupResponse> getAll() {
        return groupService.getAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public GroupResponse getById(@PathVariable UUID id) {
        return groupService.getById(id);
    }

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public GroupResponse getByName(@RequestParam(name = "name") String name) {
        return groupService.getByName(name);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public GroupResponse create(@RequestBody @Valid GroupCreateRequest groupRequest) {
        return groupService.create(groupRequest);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public GroupResponse update(@PathVariable UUID id, @RequestBody @Valid GroupEditRequest groupRequest) {
        return groupService.update(id, groupRequest);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public GroupResponse delete(@PathVariable UUID id) {
        return groupService.delete(id);
    }
}