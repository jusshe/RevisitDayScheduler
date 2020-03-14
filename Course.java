import java.util.LinkedList;

public class Course {
    private final String name;
    private final LinkedList<Student> bucket1 = new LinkedList<Student>();
    private final LinkedList<Student> bucket2 = new LinkedList<Student>();
    private int cap;

    Course(String name) {
        this.name = name;
    }

    public void setCap(int cap) {
        this.cap = cap;
    }

    public int getCap() {
        return this.cap;
    }

    public void addStudent(int bucket, Student student) {
        if (bucket < 1 || bucket > 2) {
            throw new IllegalArgumentException("Invalid time: " + bucket);
        }

        if (bucket == Scheduler.COURSE_TIME_1) {
            bucket1.add(student);
        } else bucket2.add(student);
    }

    public LinkedList<Student> getSection(int classnum) {
        if (classnum < 1 || classnum > 2) {
            throw new IllegalArgumentException("Invalid class: " + classnum);
        }

        if (classnum == Scheduler.COURSE_TIME_1) {
            return bucket1;
        } else return bucket2;
    }

    public String toString() {
        return name;
    }
}
