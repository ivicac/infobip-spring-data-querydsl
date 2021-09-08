package com.infobip.spring.data.jdbc.embedded;

import com.infobip.spring.data.jdbc.TestBase;
import com.querydsl.core.types.Projections;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.junit.jupiter.api.*;

import java.time.ZoneOffset;
import java.util.*;

import static com.infobip.spring.data.jdbc.embedded.QPersonWithEmbeddedFirstAndLastName.personWithEmbeddedFirstAndLastName;
import static org.assertj.core.api.BDDAssertions.then;

@AllArgsConstructor
public class PersonWithEmbeddedFirstAndLastNameRepositoryTest extends TestBase {

    private static final TimeZone oldTimeZone = TimeZone.getDefault();

    private final PersonWithEmbeddedFirstAndLastNameRepository repository;

    @BeforeAll
    void setupTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
    }

    @AfterAll
    void cleanUpTimeZone() {
        TimeZone.setDefault(oldTimeZone);
    }

    @Test
    void shouldFindAll() {

        // given
        PersonWithEmbeddedFirstAndLastName johnDoe = givenSavedPerson("John", "Doe");
        PersonWithEmbeddedFirstAndLastName johnyRoe = givenSavedPerson("Johny", "Roe");
        PersonWithEmbeddedFirstAndLastName janeDoe = givenSavedPerson("Jane", "Doe");

        // when
        List<PersonWithEmbeddedFirstAndLastName> actual = repository.findAll();

        then(actual).containsExactlyInAnyOrder(johnDoe, johnyRoe, janeDoe);
    }

    @Test
    void shouldFindAllWithPredicate() {

        // given
        PersonWithEmbeddedFirstAndLastName johnDoe = givenSavedPerson("John", "Doe");
        PersonWithEmbeddedFirstAndLastName johnyRoe = givenSavedPerson("Johny", "Roe");
        givenSavedPerson("Jane", "Doe");

        // when
        List<PersonWithEmbeddedFirstAndLastName> actual = repository.findAll(
                personWithEmbeddedFirstAndLastName.firstName.in("John", "Johny"));

        then(actual).containsOnly(johnDoe, johnyRoe);
    }

    @Test
    void shouldQuery() {

        // given
        PersonWithEmbeddedFirstAndLastName johnDoe = givenSavedPerson("John", "Doe");
        givenSavedPerson("Johny", "Roe");
        givenSavedPerson("Jane", "Doe");
        givenSavedPerson("John", "Roe");
        givenSavedPerson("Janie", "Doe");

        // when
        List<PersonWithEmbeddedFirstAndLastName> actual = repository.query(query -> query
                .select(repository.entityProjection())
                .from(personWithEmbeddedFirstAndLastName)
                .where(personWithEmbeddedFirstAndLastName.firstName.in("John", "Jane"))
                .orderBy(personWithEmbeddedFirstAndLastName.firstName.asc(),
                         personWithEmbeddedFirstAndLastName.lastName.asc())
                .limit(1)
                .offset(1)
                .fetch());

        then(actual).containsOnly(johnDoe);
    }

    @Test
    void shouldQueryOne() {

        // given
        PersonWithEmbeddedFirstAndLastName johnDoe = givenSavedPerson("John", "Doe");
        givenSavedPerson("Johny", "Roe");
        givenSavedPerson("Jane", "Doe");
        givenSavedPerson("John", "Roe");
        givenSavedPerson("Janie", "Doe");

        // when
        Optional<PersonWithEmbeddedFirstAndLastName> actual = repository.queryOne(query -> query
                .select(repository.entityProjection())
                .from(personWithEmbeddedFirstAndLastName)
                .where(personWithEmbeddedFirstAndLastName.firstName.in("John", "Jane"))
                .orderBy(personWithEmbeddedFirstAndLastName.firstName.asc(),
                         personWithEmbeddedFirstAndLastName.lastName.asc())
                .limit(1)
                .offset(1));

        then(actual).contains(johnDoe);
    }

    @Test
    void shouldQueryMany() {

        // given
        PersonWithEmbeddedFirstAndLastName johnDoe = givenSavedPerson("John", "Doe");
        givenSavedPerson("Johny", "Roe");
        givenSavedPerson("Jane", "Doe");
        givenSavedPerson("John", "Roe");
        givenSavedPerson("Janie", "Doe");

        // when
        List<PersonWithEmbeddedFirstAndLastName> actual = repository.queryMany(query -> query
                .select(repository.entityProjection())
                .from(personWithEmbeddedFirstAndLastName)
                .where(personWithEmbeddedFirstAndLastName.firstName.in("John", "Jane"))
                .orderBy(personWithEmbeddedFirstAndLastName.firstName.asc(),
                         personWithEmbeddedFirstAndLastName.lastName.asc())
                .limit(1)
                .offset(1));

        then(actual).containsOnly(johnDoe);
    }

    @Test
    void shouldProject() {

        // given
        PersonWithEmbeddedFirstAndLastName johnDoe = givenSavedPerson("John", "Doe");

        // when
        List<PersonProjection> actual = repository.query(query -> query
                .select(Projections.constructor(PersonProjection.class,
                                                personWithEmbeddedFirstAndLastName.firstName,
                                                personWithEmbeddedFirstAndLastName.lastName))
                .from(personWithEmbeddedFirstAndLastName)
                .fetch());

        // then
        then(actual).containsExactly(new PersonProjection(johnDoe.getFirstAndLastName().getFirstName(),
                                                          johnDoe.getFirstAndLastName().getLastName()));
    }

    @Test
    void shouldUpdate() {

        // given
        givenSavedPerson("John", "Doe");
        givenSavedPerson("Johny", "Roe");
        givenSavedPerson("Jane", "Doe");

        // when
        Long actual = repository.update(query -> query
                .set(personWithEmbeddedFirstAndLastName.firstName, "John")
                .where(personWithEmbeddedFirstAndLastName.firstName.eq("Johny"))
                .execute());

        then(actual).isEqualTo(1);
        then(repository.findAll()).extracting(person -> person.getFirstAndLastName().getFirstName())
                                  .containsExactlyInAnyOrder("John", "John", "Jane")
                                  .hasSize(3);
    }

    @Test
    void shouldDelete() {

        // given
        givenSavedPerson("John", "Doe");
        givenSavedPerson("Johny", "Roe");
        PersonWithEmbeddedFirstAndLastName janeDoe = givenSavedPerson("Jane", "Doe");
        givenSavedPerson("John", "Roe");

        // when
        long actual = repository.deleteWhere(personWithEmbeddedFirstAndLastName.firstName.like("John%"));

        then(repository.findAll()).containsExactly(janeDoe);
        then(actual).isEqualTo(3L);
    }

    @Value
    public static class PersonProjection {

        private final String firstName;
        private final String lastName;
    }

    private PersonWithEmbeddedFirstAndLastName givenSavedPerson(String firstName, String lastName) {
        return repository.save(new PersonWithEmbeddedFirstAndLastName(null, new FirstAndLastName(firstName, lastName),
                                                                      BEGINNING_OF_2021));
    }
}