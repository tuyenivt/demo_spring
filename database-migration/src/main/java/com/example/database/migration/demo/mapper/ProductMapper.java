package com.example.database.migration.demo.mapper;

import com.example.database.migration.demo.entity.Product;
import com.example.database.migration.oldDemo.entity.OldProduct;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface ProductMapper {

    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "vendor", constant = "ABC")
    @Mapping(target = "inStock", source = "quality", defaultValue = "0L")
    Product from(OldProduct source, @Context int timezoneOffsetHours);

    @AfterMapping
    default void adjustTimezone(@MappingTarget Product target, OldProduct source, @Context int timezoneOffsetHours) {
        if (source.getUpdatedAt() != null) {
            target.setUpdatedAt(source.getUpdatedAt().plusHours(timezoneOffsetHours));
        }
    }
}
