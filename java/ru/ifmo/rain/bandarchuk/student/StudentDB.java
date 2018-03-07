package ru.ifmo.rain.bandarchuk.student;

import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentQuery {

    private static final Comparator<Student> STUDENT_BY_NAME_COMPARATOR = Comparator.comparing(Student::getLastName).
        thenComparing(Student::getFirstName).
        thenComparing(Student::getId);

    private static final BinaryOperator<String> MINIMAL_STRING = BinaryOperator.minBy(String::compareTo);

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return mappedList(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return mappedList(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return mappedList(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return mappedList(students, StudentDB::getFullName);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return map(students, Student::getFirstName).collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.stream()
            .min(Comparator.comparingInt(Student::getId))
            .map(Student::getFirstName).orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortedList(students, Comparator.comparingInt(Student::getId));
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortedList(students, STUDENT_BY_NAME_COMPARATOR);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return sortedList(filter(students, student -> student.getFirstName().equals(name)), STUDENT_BY_NAME_COMPARATOR);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return sortedList(filter(students, student -> student.getLastName().equals(name)), STUDENT_BY_NAME_COMPARATOR);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return sortedList(filter(students, student -> student.getGroup().equals(group)), STUDENT_BY_NAME_COMPARATOR);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return filter(students, student -> student.getGroup()
            .equals(group))
            .collect(Collectors.toMap(Student::getLastName, Student::getFirstName, MINIMAL_STRING));
    }

    private static String getFullName(Student student) {
        return student.getFirstName() + " " + student.getLastName();
    }

    private Stream<String> map(Stream<Student> students, Function<Student, String> mapper) {
        return students.map(mapper);
    }

    private Stream<String> map(Collection<Student> students, Function<Student, String> mapper) {
        return map(students.stream(), mapper);
    }

    private List<String> mappedList(Stream<Student> students, Function<Student, String> mapper) {
        return map(students, mapper).collect(Collectors.toList());
    }

    private List<String> mappedList(Collection<Student> students, Function<Student, String> mapper) {
        return mappedList(students.stream(), mapper);
    }

    private Stream<Student> filter(Collection<Student> students, Predicate<Student> predicate) {
        return filter(students.stream(), predicate);
    }

    private Stream<Student> filter(Stream<Student> students, Predicate<Student> predicate) {
        return students.filter(predicate);
    }

    private Stream<Student> sort(Stream<Student> students, Comparator<Student> comparator) {
        return students.sorted(comparator);
    }

    private List<Student> sortedList(Stream<Student> students, Comparator<Student> comparator) {
        return sort(students, comparator).collect(Collectors.toList());
    }

    private List<Student> sortedList(Collection<Student> students, Comparator<Student> comparator) {
        return sortedList(students.stream(), comparator);
    }

}
