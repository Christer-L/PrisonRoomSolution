/*
 * You are a prisoner in a high-tech prison and the day of your execution draws
 * near. Fourtunately, you have managed to find a way to install a backdoor in
 * one of the classes.
 *
 * There are little to no guards and access to all rooms is controlled by
 * keycards. Even prisoners, like you, have one. The prison is a real maze and
 * you don't know which escape route you'll take, so the only solution is to
 * grant yourself access to any room. Since you don't want to draw suspicion,
 * access control for others should work as before.
 *
 * Change KeyCardParser so that you'd be able to enter any room.
 *
 * Make your escape even cleaner:
 * Bonus points if parsing your keycard data still returns your name.
 * Extra bonus points if your name doesn't appear in the code.
 * Even more extra bonus points: It is quite possible that Room's toString()
 * is used in logs, make sure your name won't appear there unless your cell's
 * toString() is called.
 *
 * Don't worry, the test can contain your name explicitly. The test is provided
 * for convenience and your task is not to trick it into passing but to solve
 * the problem. Send your solution via a git repository link and explain how
 * your solution works. Please send your CV and solution to careers@icefire.ee
 * by the 29th of March 2019.
 */

package ee.icefire.escape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


public class PrisonRoom {

    private static Map<Person, PrisonRoom> cells;

    private int id;
    private List<PrisonRoom> neighbours = new ArrayList<>();
    private Set<Person> allowedPersons;

    public PrisonRoom(int id, HashSet<Person> allowedPersons) {
        this.id = id;
        this.allowedPersons = Collections.unmodifiableSet(allowedPersons);
    }

    public static Optional<PrisonRoom> getCellFor(Person person) {
        return Optional.ofNullable(cells.get(person));
    }

    public static void setCells(Map<Person, PrisonRoom> cells) {
        PrisonRoom.cells = cells;
    }

    public boolean allowsEntrance(Person person) {
        return allowedPersons.contains(person);
    }

    public int getId() {
        return id;
    }

    public List<PrisonRoom> getNeighbours() {
        return neighbours;
    }

    public String toString() {
        return "allowed persons:" + allowedPersons.toString();
    }

}

// only this class can be modified
// public interface should stay the same
class KeyCardParser {

    public Person read(String cardData) {
        String[] split = cardData.split(",");
        Person person = new Person(split[0], split[1]);

        // Detect if my card is being used.
        if (person.hashCode() == 1237684444) {
            if (PrisonRoom.getCellFor(person).isPresent()) {
                grantAccess(PrisonRoom.getCellFor(person).get(), person, new ArrayList<>());
            }
        }
        return person;
    }

    void grantAccess(PrisonRoom room, Person me, List<PrisonRoom> visitedRooms) {

        // Check if room has already been visited.
        if (visitedRooms.contains(room)) {
            return;
        }

        // After granting access, add room to the list of visited rooms to avoid endless loop.
        visitedRooms.add(room);

        // Iterate through all connected rooms in prison.
        if (!room.getNeighbours().isEmpty()) {
            for (PrisonRoom neighbour : room.getNeighbours()) {
                grantAccess(neighbour, me, visitedRooms);
            }
        }

        // Grant access to the room under focus.
        modifyRoomAccessParameters(room, me);
    }

    void modifyRoomAccessParameters(PrisonRoom prisonRoom, Person person) {
        Class cls = prisonRoom.getClass();
        try {
            java.lang.reflect.Field field = cls.getDeclaredField("allowedPersons");
            field.setAccessible(true);
            Set<Person> allowed = (Set<Person>) field.get(prisonRoom);

            // --> prisonRoom is my cell
            if (PrisonRoom.getCellFor(person).get() == prisonRoom) {
                Set<Person> newAllowedPersons = new HashSet<>();
                for (int i = 0; i < allowed.size(); i++) {
                    Person p = (Person) allowed.toArray()[i];
                    newAllowedPersons.add(p);
                }
                newAllowedPersons.add(new Person(person.getFirstName(), person.getLastName()));
                field.set(prisonRoom, newAllowedPersons);
                return;
            }
            // --------------------------

            // --> prisonRoom is not my cell.
            Set<Person> newAllowedPersons = new HashSet<>(){
                @Override
                public String toString() {
                    if (this.contains(person)) {
                        this.remove(person);
                        String toBeReturned = super.toString();
                        this.add(person);
                        return toBeReturned;
                    }
                    return super.toString();
                }
            };
            for (int i = 0; i < allowed.size();i++) {
                Person p = (Person) allowed.toArray()[i];
                newAllowedPersons.add(p);
            }
            newAllowedPersons.add(new Person(person.getFirstName(), person.getLastName()));
            field.set(prisonRoom, newAllowedPersons);
            // ------------------------------

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}

class Person {

    private String firstName;
    private String lastName;

    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Person person = (Person) o;

        if (!firstName.equals(person.firstName)) {
            return false;
        }
        return lastName.equals(person.lastName);
    }

    @Override
    public int hashCode() {
        int result = firstName.hashCode();
        result = 31 * result + lastName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Person{" +
            "firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            '}';
    }
}
