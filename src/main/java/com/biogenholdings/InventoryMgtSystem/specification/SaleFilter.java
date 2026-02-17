package com.biogenholdings.InventoryMgtSystem.specification;

import com.biogenholdings.InventoryMgtSystem.models.Sale;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import org.springframework.core.type.filter.AspectJTypeFilter;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class SaleFilter {

    public static Specification<Sale> byFilter(String searchValue){

        return (root, query, criteriaBuilder) -> {

            //if search value is null return all the sales
            if (searchValue == null || searchValue.isEmpty()){
                return criteriaBuilder.conjunction();
            }

            String searchPattern  = "%" + searchValue.toLowerCase() + "%";

            //create a list to hold my predicates
            List<Predicate> predicates = new ArrayList<>();

            //search within sale fields
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("note")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("status").as(String.class)), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("saleType").as(String.class)), searchPattern));

            //safely join to check the user fields
            if(root.getJoins().stream().noneMatch(j -> j.getAttribute().getName().equals("user"))){
                root.join("user", JoinType.LEFT);
            }

            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("user", JoinType.LEFT).get("name")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("user", JoinType.LEFT).get("email")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("user", JoinType.LEFT).get("phoneNumber")), searchPattern));

            //safely join to check the supplier fields
            if(root.getJoins().stream().noneMatch(j -> j.getAttribute().getName().equals("supplier"))){
                root.join("supplier", JoinType.LEFT);
            }

            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("supplier", JoinType.LEFT).get("name")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("supplier", JoinType.LEFT).get("phoneNumber")), searchPattern));

            //safely join to check the product fields
            if(root.getJoins().stream().noneMatch(j -> j.getAttribute().getName().equals("product"))){
                root.join("product", JoinType.LEFT);
            }

            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("product", JoinType.LEFT).get("name")), searchPattern));
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("product", JoinType.LEFT).get("description")), searchPattern));

            //safely join to check the category fields
            if(root.getJoins().stream().noneMatch(j -> j.getAttribute().getName().equals("product")) &&
            root.join("product").getJoins().stream().noneMatch(j -> j.getAttribute().getName().equals("category"))){
                root.join("product", JoinType.LEFT).join("category", JoinType.LEFT);
            }

            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.join("product", JoinType.LEFT).join("category", JoinType.LEFT).get("name")), searchPattern));

            //combine all predicates with OR
            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));

        };
    }

    public static Specification<Sale> byMonthAndYear(int month, int year){
        return (root, query, criteriaBuilder) -> {
            Expression<Integer> monthExpression = criteriaBuilder.function("month", Integer.class, root.get("createdAt"));
            Expression<Integer> yearExpression = criteriaBuilder.function("year", Integer.class, root.get("createdAt"));

            Predicate monthPredicate = criteriaBuilder.equal(monthExpression, month);
            Predicate yearPredicate = criteriaBuilder.equal(yearExpression, year);

            return  criteriaBuilder.and(monthPredicate, yearPredicate);
        };
    }
}
