package kg.astra.crew;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "crew")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Crew {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    /** Comma-separated in the DB, exposed as a Set. */
    @Column(nullable = false)
    private String skills;

    @Column(name = "dose_budget_micro_sv", nullable = false)
    private long doseBudgetMicroSv;

    public Set<String> skillSet() {
        if (skills == null || skills.isBlank()) return Set.of();
        return Arrays.stream(skills.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }
}
