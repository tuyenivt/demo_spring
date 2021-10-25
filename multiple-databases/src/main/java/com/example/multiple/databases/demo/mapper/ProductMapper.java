package com.example.multiple.databases.demo.mapper;

import com.example.multiple.databases.demo.entity.Product;
import com.example.multiple.databases.oldDemo.entity.OldProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ProductMapper {

    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(target = "updatedAt", expression = "java(source.getUpdatedAt().plusHours(-7))")
    @Mapping(target = "vendor", constant = "ABC")
    @Mapping(target = "inStock", source = "quality", defaultValue = "0L")
    Product from(OldProduct source);
}
