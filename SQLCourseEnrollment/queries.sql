--Print the names of professors who work in departments that have fewer than 50 PhD students.
   SELECT pname
     FROM prof P,
          dept D
    WHERE P.dname = D.dname
      AND numphds < 50;

--Print the names of the students with the lowest GPA
   with lowestGPA as (
     SELECT min(gpa)
          FROM student
   )
   SELECT sname
     FROM student IN ( lowestGPA);

--For each Computer Sciences class, print the class number, section number, and the average gpa of the students enrolled in the class section.
   SELECT cno,
          sectno,
          avg(grade) AS avgGPA
     FROM enroll
     where dname = 'Computer Sciences'
 GROUP BY cno,
          sectno;

--Print the names and section numbers of all sections with more than six students enrolled in them.
   SELECT cname,
          sectno
     FROM course C,
          section S,
          enroll E
    WHERE c.cno = section.cno
      AND c.cno = e.cno
      AND count(sid) > 6;

--Print the name(s) and sid(s) of the student(s) enrolled in the most sections.
   SELECT sname,
          sid
     FROM student s,
          enroll e
    WHERE s.sid = e.sid
      AND count(e.sid) AS amount
      AND max(amount);

--Print the names of departments that have one or more majors who are under 18 years old.
   SELECT dname
     FROM dept d,
          major m,
          student s
    WHERE d.dname = m.dname
      AND m.sid = s.sid
      AND age < 18;

--Print the names and majors of students who are taking one of the College Geometry courses.
   SELECT sname,
          c.dname
     FROM student s,
          major m,
          enroll e,
          course c
    WHERE s.sid = m.sid
      AND s.sid = e.sid
      AND e.cno = c.cno
      AND cname = "% Geometry";

--For those departments that have no major taking a College Geometry course print the department name and the number of PhD students in the department.
   SELECT c.dname,
          numphds
     FROM dept d,
          major m,
          course c,
          enroll e
    WHERE d.dname = m.dname
      AND m.dname = e.dname
      AND e.cno = c.cno
      AND cname != '% Geometry';

--Print the names of students who are taking both a Computer Sciences course and a Mathematics course.
   SELECT sname
     FROM students s,
          course c,
          enroll e
    WHERE s.sid = e.sid
      AND e.cno = c.cno
      AND c.cname in (select cname from course where cname = 'Computer Science %')
      AND c.cname in (select cname from course where cname = 'Mathematics %');

--Print the age difference between the oldest and the youngest Computer Sciences major.
   SELECT (max(age) - min(age)) AS differneceInAge
     FROM student s,
          major m
    WHERE s.sid = m.sid
      AND dname = 'Comptuer Sciences';

--For each department that has one or more majors with a GPA under 1.0, print the name of the department and the average GPA of its majors.
     WITH majorsWithLowGPA (student) AS (
             SELECT sid
               FROM students
              WHERE gpa < 1.0
          )
   SELECT dname,
          avg(s.gpa)
     FROM major m,
          student s
    WHERE m.sid = s.sid
      left outer join majorsWithLowGPA on s.sid = majorsWithLowGPA.sid
 GROUP BY (dname);

--Print the ids, names and GPAs of the students who are currently taking all the Civil Engineering courses.
     WITH allCivilEngCourses (course) AS (
             SELECT cno, count (cno) as all3
               FROM course
              WHERE dname = 'Civil Engineering'
              group by cno
          )
   SELECT s.sid,
          s.sname,
          gpa
     FROM student s,
          enroll e,
          course c
    WHERE s.sid = e.sid
      AND e.cno = c.cno
      AND s.sid IN (select e2.sid from enroll e2, allCivilEngCourses ACEC where e2.cno = ACEC.cno and all3 = 3);