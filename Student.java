import java.util.Comparator;

public class Student {
    private final String name;
    private Course course1;
    private Course course2;
    private final Course[] preferences = new Course[5];
    private boolean checkedPref = false;

    public Student(String name) {
        this.name = name;
    }

    static class studentComparator implements Comparator<Student> {
        public int compare(Student one, Student two) {
            return one.getPrioritySum() - two.getPrioritySum();
        }
    }

    public int getPrioritySum() {
        return getFirstCPriority() + getSecondCPriority();
    }

    public int getFirstCPriority() {
        for (int i = 0; i < preferences.length; i++) {
            if (preferences[i] == this.getCourse(Scheduler.COURSE_TIME_1)) {
                return i + 1;
            }
        }

        throw new RuntimeException("Course 1 is not in preferences array.");
    }

    public int getSecondCPriority() {
        for (int i = 0; i < preferences.length; i++) {
            if (preferences[i] == this.getCourse(Scheduler.COURSE_TIME_2)) {
                return i + 1;
            }
        }

        throw new RuntimeException("Course 2 is not in preferences array.");
    }

    public void setCourse(int courseNum, Course course) {
        if (course == null) throw new IllegalArgumentException("null course");
        if (courseNum > 2 || courseNum < 1) {
            throw new IllegalArgumentException("courseNum outside of range (1-2)");
        }
        if (courseNum == Scheduler.COURSE_TIME_1) {
            if (course1 != null) {
                course1.getSection(Scheduler.COURSE_TIME_1).remove(this);
            }
            course1 = course;
            course1.addStudent(Scheduler.COURSE_TIME_1, this);

        } else {
            if (course2 != null) {
                course2.getSection(Scheduler.COURSE_TIME_2).remove(this);
            }
            course2 = course;
            course2.addStudent(Scheduler.COURSE_TIME_2, this);
        }
    }

    public Course getCourse(int courseNum) {
        if (isNull(courseNum)) throw new RuntimeException("course not yet set");

        if (courseNum == Scheduler.COURSE_TIME_1) return course1;
        else return course2;
    }

    public boolean isNull(int courseNum) {
        if (courseNum > 2 || courseNum < 1) {
            throw new IllegalArgumentException("courseNum outside of range (1-2)");
        }
        if (courseNum == Scheduler.COURSE_TIME_1) return course1 == null;
        else return course2 == null;
    }

    public String getName() {
        return name;
    }

    public String getClasses() {
        if (isNull(Scheduler.COURSE_TIME_1)) throw new RuntimeException("Course 1 is not yet set");
        if (isNull(Scheduler.COURSE_TIME_2)) throw new RuntimeException("Course 2 is not yet set");

        return course1 + ", " + course2;
    }

    public void setPreferences(Course course, int priority) {
        if (priority < 0 || priority > 4) {
            throw new IllegalArgumentException("Priority is illegal: " + priority);
        }

        preferences[priority] = course;
    }

    public Course[] getPreferences() {
        if (!checkedPref) {
            for (int i = 0; i < 5; i++) {
                if (preferences[i] == null) {
                    throw new RuntimeException(this.getName() + " did not set his/her preference " + (i + 1));
                }
            }
            checkedPref = true;
        }
        return preferences;
    }
}
