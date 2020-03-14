import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

@SuppressWarnings("unchecked")
public class Scheduler {
    public static final int COURSE_TIME_1 = 1;
    public static final int COURSE_TIME_2 = 2;

    public static void main(String[] args) throws IOException {
        // collect # of students and classes
        Scanner user = new Scanner(System.in);
        System.out.println("How many students responded to the survey?");
        int numStudents = Integer.parseInt(user.nextLine());
        System.out.println("How many classes are available for students to visit?");
        int numClasses = Integer.parseInt(user.nextLine());

        Student[] students = new Student[numStudents];
        Course[] courses = new Course[numClasses];

        System.out.println("Write the complete name of the exported survey file: ");

        File survey = new File(user.nextLine());
        Scanner fileScan;
        try {
            fileScan = new Scanner(survey);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(
                    "\nFile not found... \nIt is either missing from the folder containing " +
                            "this program or was not entered properly. Be sure to include the suffix (.csv)");
        }

        String classes = fileScan.nextLine();
        for (int i = 0; i < numClasses; i++) {
            String crs = classes.substring(classes.indexOf("[") + 1, classes.indexOf("]"));
            courses[i] = new Course(crs);

            if (i == numClasses - 1) continue;
            classes = classes.substring(classes.indexOf("]") + 1);
        }

        int whichStudent = 0;
        while (fileScan.hasNext()) {
            String line = fileScan.nextLine();
            line = updateLine(line);
            line = updateLine(line);

            // extract name and update line
            String name = readInQuotes(line);
            line = updateLine(line);
            students[whichStudent] = new Student(name);

            int whichClass = 0;
            while (whichClass < numClasses) {
                String choice = readInQuotes(line);
                line = updateLine(line);
                if (!choice.equals("")) {
                    int rank = Integer.parseInt(choice.substring(0, 1));

                    students[whichStudent].setPreferences(courses[whichClass], rank - 1);
                }
                whichClass++;
            }
            whichStudent++;
        }
        fileScan.close();

        System.out.println("At the most, how many visiting students can be in one class at one time?");
        int standardCap = Integer.parseInt(user.nextLine());

        // Prompt the user for classes with multiple sections, if so update courses cap to inputCap*input
        System.out.println("Do any classes have multiple sections? (y/n)");
        String answer = user.nextLine();

        if (answer.equals("y")) {
            System.out.println("How many classes have multiple sections?");
            int classesWithManySections = Integer.parseInt(user.nextLine());
            for (int i = 0; i < classesWithManySections; i++) {
                adjustCap(user, courses, standardCap);
            }
        }
        user.close();

        // set classes without adjusted caps to cap = inputCap
        for (Course crs : courses) {
            if (crs.getCap() == 0) {
                crs.setCap(standardCap);
            }
        }


        // place each student in his/her first choice first period and their second choice second
        for (Student student : students) {
            student.setCourse(COURSE_TIME_1, student.getPreferences()[0]);
            student.setCourse(COURSE_TIME_2, student.getPreferences()[1]);
        }

        for (Course course : courses) {
            for (Student student : (LinkedList<Student>) course.getSection(COURSE_TIME_1).clone()) {
                swapWithinClasses(course, student);
            }
        }

        // move students in courses with sections that are beyond their cap
        for (Course course : courses) {
            if (course.getSection(COURSE_TIME_1).size() > course.getCap()) {
                removeTillUnderCap(course, COURSE_TIME_1);
            }
            if (course.getSection(COURSE_TIME_2).size() > course.getCap()) {
                removeTillUnderCap(course, COURSE_TIME_2);
            }
        }

        // TESTING
        //printSchedule();

        // create schedule file
        File folder = new File("./The Schedule and Analytics");
        if (!folder.mkdir()) {
            throw new RuntimeException("Directory could not be created. Try checking that " +
                    "The Schedule and Analytics folder doesn't already exist.");
        }
        File scheduleByClass = new File(folder, "Schedule by Class");
        if (!scheduleByClass.createNewFile()) {
            throw new RuntimeException("File couldn't be created.");
        }
        FileWriter ClassSchWriter = new FileWriter(scheduleByClass);
        for (Course course : courses) {
            ClassSchWriter.write(course.toString() + " #1: ");
            for (Student student : course.getSection(COURSE_TIME_1)) {
                ClassSchWriter.write(student.getName() + " | ");
            }
            ClassSchWriter.write("\n");
            ClassSchWriter.write(course.toString() + " #2: ");
            for (Student student : course.getSection(COURSE_TIME_2)) {
                ClassSchWriter.write(student.getName() + " | ");
            }
            ClassSchWriter.write("\n\n");
        }
        ClassSchWriter.close();

        File scheduleByStudent = new File(folder, "Schedule by Student");
        if (!scheduleByStudent.createNewFile()) {
            throw new RuntimeException("File couldn't be created.");
        }

        FileWriter studentSchWriter = new FileWriter(scheduleByStudent);
        for (Student student : students) {
            studentSchWriter.write(student.getName() + ": " + student.getClasses() + "\n");
        }
        studentSchWriter.close();

        // create analytics file: percentage of students with each priority and priorities for each students
        File analytics = new File("./The Schedule and Analytics/Analytics");
        if (!analytics.createNewFile()) {
            throw new RuntimeException("File couldn't be created.");
        }
        FileWriter analyticsWriter = new FileWriter(analytics);

        int studentCount = 0;
        for (int i = 3; i < 11; i++) { // possible priority sums: 3 - 11
            for (Student student : students) {
                if (student.getPrioritySum() == i) {
                    studentCount++;
                }
            }
            analyticsWriter.write("% of Students with Priority sum = " + i + ": "
                    + (double) studentCount / students.length * 100 + "%\n");
            studentCount = 0;
        }

        analyticsWriter.write("\nIndividual Priorities (Student: Class 1 Priority, Class 2 Priority)\n");

        Arrays.sort(students, new Student.studentComparator());
        for (int i = students.length - 1; i >= 0; i--) {
            analyticsWriter.write(students[i].getName() + ": ");
            analyticsWriter.write(students[i].getFirstCPriority() + ", "
                    + students[i].getSecondCPriority() + "\n");
        }

        analyticsWriter.close();
    }

    //------------------------------------------------------------------------------------------//
    private static void removeTillUnderCap(Course course, int section) {
        int priority = 2;
        while (course.getSection(section).size() > course.getCap()) {
            if (priority == 5) {
                throw new RuntimeException("Scheduler failed. Could not find students to move from "
                        + course.toString() + ", time " + section);
            }

            LinkedList<Student> potenSwitch = new LinkedList<Student>();
            for (Student student : course.getSection(section)) {
                if (student.getPreferences()[priority].getSection(section).size()
                        < student.getPreferences()[priority].getCap()) {
                    potenSwitch.add(student);
                }
            }

            // move student with best classes currently
            Student toSwitch = potenSwitch.getFirst();
            for (Student student : potenSwitch) {
                if (student.getPrioritySum() < toSwitch.getPrioritySum()) {
                    toSwitch = student;
                }
            }
            toSwitch.setCourse(section, toSwitch.getPreferences()[priority]);

            if (potenSwitch.size() <= 1) priority++;
        }
    }

    private static void adjustCap(Scanner user, Course[] courses, int inputCap) {
        System.out.println(
                "Write the name of a class with multiple sections (exactly as it appears in the survey): ");
        String course = user.nextLine();
        System.out.println("How many sections of " + course + " are there?");
        int sections = Integer.parseInt(user.nextLine());

        boolean found = false;
        for (Course crs : courses) {
            if (crs.toString().equals(course)) {
                crs.setCap(sections * inputCap);
                found = true;
            }
        }

        if (!found) {
            System.out.println("Course not found in survey. Returning...\n");
            adjustCap(user, courses, inputCap);
        }
    }

    private static void swapWithinClasses(Course course, Student student) {
        int currentSum = course.getSection(COURSE_TIME_1).size()
                + student.getCourse(COURSE_TIME_2).getSection(COURSE_TIME_2).size();
        int potentialSum = student.getCourse(COURSE_TIME_2).getSection(COURSE_TIME_1).size()
                + student.getCourse(COURSE_TIME_1).getSection(COURSE_TIME_2).size() + 2;
        if (potentialSum < currentSum) {
            Course course1 = student.getCourse(COURSE_TIME_1);
            student.setCourse(COURSE_TIME_1, student.getCourse(COURSE_TIME_2));
            student.setCourse(COURSE_TIME_2, course1);
        }
    }

    private static String readInQuotes(String line) {
        line = line.substring(1);
        return line.substring(0, line.indexOf('"'));
    }

    private static String updateLine(String line) {
        int cutoff = line.indexOf('"', line.indexOf('"') + 1) + 1;
        if (cutoff == line.length()) {
            return line;
        }
        return line.substring(cutoff + 1);
    }

    /* TESTING
    private static void printSchedule(Course[] courses) {
        for (Course course : courses) {
            System.out.print(course.toString() + " #1: ");
            for (Student student : course.getSection(1)) {
                System.out.print(student.getName() + " | ");
            }
            System.out.println();
            System.out.print(course.toString() + " #2: ");
            for (Student student : course.getSection(2)) {
                System.out.print(student.getName() + " | ");
            }
            System.out.println();
    }*/
}
