/*
package ru.impuls.helpdeskservice.mapper.impl;

@Component
public class SubgroupMapperImpl implements SubgroupMapper {

    @Override
    public SubgroupDto toDto(Subgroup subgroup) {
        if (subgroup == null) return null;

        return SubgroupDto.builder()
                .id(subgroup.getId())
                .name(subgroup.getName())
                .build();
    }

    @Override
    public SubgroupEventDto toEventDto(Subgroup subgroup) {
        if (subgroup == null) return null;

        var group = GroupForSubDto.builder()
                .id(subgroup.getGroup().getId())
                .name(subgroup.getGroup().getName())
                .build();

        return SubgroupEventDto.builder()
                .id(subgroup.getId())
                .name(subgroup.getName())
                .group(group)
                .build();
    }

    @Override
    public Subgroup toEntity(SubgroupDto subgroupDto) {
        return null;
    }

    @Override
    public void toEntity(Subgroup subgroup, SubgroupEditDto subgroupDto) {
        subgroup.setName(subgroupDto.getName());
    }

    @Override
    public Subgroup toEntity(SubgroupEditDto subgroupDto, Group group) {
        if(subgroupDto == null) return null;

        return Subgroup.builder()
                .name(subgroupDto.getName())
                .group(group)
                .build();
    }
}
*/
