package com.damocles.fleet.fleetmanagementsystembackend.util;

import com.damocles.fleet.fleetmanagementsystembackend.domain.Transport;
import com.damocles.fleet.fleetmanagementsystembackend.domain.TransportStatus;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TransportSpecifications {

    public static Specification<Transport> withFilters(
            TransportStatus status,
            Long driverId,
            Long vehicleId,
            Instant from,
            Instant to,
            String q
    ) {
        return Specification.allOf(
                hasStatus(status),
                hasDriver(driverId),
                hasVehicle(vehicleId),
                hasDateRange(from, to),
                matchesQuery(q)
        );
    }

    public static Specification<Transport> hasStatus(TransportStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Transport> hasDriver(Long driverId) {
        return (root, query, cb) ->
                driverId == null ? null : cb.equal(root.get("driver").get("userId"), driverId);
    }

    public static Specification<Transport> hasVehicle(Long vehicleId) {
        return (root, query, cb) ->
                vehicleId == null ? null : cb.equal(root.get("vehicle").get("id"), vehicleId);
    }

    public static Specification<Transport> hasDateRange(Instant from, Instant to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;

            if (from != null && to != null)
                return cb.between(root.get("plannedStartAt"), from, to);

            if (from != null)
                return cb.greaterThanOrEqualTo(root.get("plannedStartAt"), from);

            return cb.lessThanOrEqualTo(root.get("plannedStartAt"), to);
        };
    }

    public static Specification<Transport> matchesQuery(String q) {
        return (root, query, cb) -> {
            if (q == null || q.trim().isEmpty()) return null;

            query.distinct(true);

            String term = q.trim().toLowerCase();
            String like = "%" + term + "%";
            List<Predicate> predicates = new ArrayList<>();

            if (term.matches("\\d+")) {
                try {
                    predicates.add(cb.equal(root.get("id"), Long.valueOf(term)));
                } catch (NumberFormatException ignored) {
                }
            }

            predicates.add(cb.like(cb.lower(root.get("status").as(String.class)), like));

            var driverJoin = root.join("driver", JoinType.LEFT);
            var userJoin = driverJoin.join("user", JoinType.LEFT);
            predicates.add(cb.like(cb.lower(cb.coalesce(userJoin.get("firstName"), "")), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(userJoin.get("lastName"), "")), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(userJoin.get("email"), "")), like));

            var vehicleJoin = root.join("vehicle", JoinType.LEFT);
            predicates.add(cb.like(cb.lower(cb.coalesce(vehicleJoin.get("licensePlate"), "")), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(vehicleJoin.get("manufacturer"), "")), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(vehicleJoin.get("model"), "")), like));

            var trailerJoin = root.join("trailer", JoinType.LEFT);
            predicates.add(cb.like(cb.lower(cb.coalesce(trailerJoin.get("licensePlate"), "")), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(trailerJoin.get("name"), "")), like));

            var pickupJoin = root.join("pickupLocation", JoinType.LEFT);
            predicates.add(cb.like(cb.lower(cb.coalesce(pickupJoin.get("city"), "")), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(pickupJoin.get("street"), "")), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(pickupJoin.get("postcode"), "")), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(pickupJoin.get("country"), "")), like));

            var deliveryJoin = root.join("deliveryLocation", JoinType.LEFT);
            predicates.add(cb.like(cb.lower(cb.coalesce(deliveryJoin.get("city"), "")), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(deliveryJoin.get("street"), "")), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(deliveryJoin.get("postcode"), "")), like));
            predicates.add(cb.like(cb.lower(cb.coalesce(deliveryJoin.get("country"), "")), like));

            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
