create table hibernate_sequence (next_val bigint);
insert into hibernate_sequence(next_val) values (1);

create table education_group
    (id bigint not null,
    title varchar(255),
    primary key (id));

create table education_group_modules
    (education_group_id bigint not null,
     modules_id bigint not null,
      primary key (education_group_id, modules_id));

create table education_group_students
    (education_group_id bigint not null,
     students_id bigint not null);

create table education_group_teachers
    (education_group_id bigint not null,
     teachers_id bigint not null);

create table message
    (id bigint primary key auto_increment,
    text varchar(500),
    theme varchar(100),
    user_email varchar(255) not null,
    user_name varchar(255));

create table module
    (id bigint not null,
    title varchar(255),
    education_group_id bigint,
    primary key (id));

create table module_tests
    (module_id bigint not null,
     tests_id bigint not null,
     primary key (module_id, tests_id));

create table module_control_work
    (module_id bigint not null,
     control_work bigint,
      control_work_key bigint not null,
       primary key (module_id, control_work_key));

create table question
    (id bigint not null,
    question varchar(255),
    primary key (id));

create table question_answers_options
    (question_id bigint not null,
    answers_options varchar(255));

create table question_correct_answer
    (question_id bigint not null,
    correct_answer bit);

create table question_students_answers
    (question_id bigint not null,
    students_answers_id bigint not null);

create table student
    (id bigint not null,
    activated_code varchar(255),
     full_name varchar(255),
     password varchar(255),
     username varchar(255),
      primary key (id));

create table student_all_marks
    (student_id bigint not null,
     all_marks integer,
     all_marks_key bigint not null,
     primary key (student_id, all_marks_key));

create table student_groups
    (student_id bigint not null,
     groups_id bigint not null);

create table students_answers
    (id bigint not null,
    is_right bit not null,
    question_id bigint,
     student_id bigint,
      primary key (id));

create table students_answers_students_answers
    (students_answers_id bigint not null,
     students_answers varchar(255));

create table teacher
    (id bigint not null,
    activated_code varchar(255),
    full_name varchar(255),
     password varchar(255),
     username varchar(255),
      primary key (id));

create table teacher_groups
    (teacher_id bigint not null,
    groups_id bigint not null);

create table test
    (id bigint not null,
    sections integer not null,
    title varchar(255),
    module_id bigint,
    filename varchar(255),
    is_file bit,
    is_control bit,
    primary key (id));

create table test_grading_system
    (test_id bigint not null,
    grading_system integer,
    grading_system_key integer not null,
    primary key (test_id, grading_system_key));

create table test_students_solving
    (test_id bigint not null,
     students_solving varchar(255),
     students_solving_key bigint not null,
     primary key (test_id, students_solving_key));

create table test_questions
    (test_id bigint not null,
    questions_id bigint not null);

create table test_students_marks
    (test_id bigint not null,
     students_marks integer,
     students_marks_key bigint not null,
     primary key (test_id, students_marks_key));

create table users
    (id bigint not null,
    activated_code varchar(255),
    full_name varchar(255),
    password varchar(255),
    username varchar(255),
    primary key (id));

create table users_roles
    (users_id bigint not null,
     roles varchar(255));


alter table education_group_modules
    add constraint UK_ncb9v0ytktx0ahi4ind9ply4p
        unique (modules_id);

alter table module_tests
    add constraint UK_tr411ue2ahmcpa6yakeggnfg3
        unique (tests_id);

alter table test_questions
    add constraint UK_qnugcdb52dsqn1p5daca885bb
        unique (questions_id);

alter table education_group_modules
    add constraint FKqpvsfp4olgt43gbcs5s6b1kdy
        foreign key (modules_id)
            references module (id);

alter table education_group_modules
    add constraint FKj5ec4pbyqxreus2km9leriwi0
        foreign key (education_group_id)
            references education_group (id);

alter table education_group_students
    add constraint FKs64xn6ajpf7fgdud9jiiadywp
        foreign key (students_id)
            references student (id);

alter table education_group_students
    add constraint FK961d294on9qbt7aox4bdd8uel
        foreign key (education_group_id)
            references education_group (id);

alter table education_group_teachers
    add constraint FK6hxt1iy5kqpxi67vuct57ji1k
        foreign key (teachers_id)
            references teacher (id);

alter table education_group_teachers
    add constraint FKlkp81mikhi13yjottv1ldl5w9
        foreign key (education_group_id)
            references education_group (id);

alter table module
    add constraint FK7rwsddqpjhbwepg870cjsj5d4
        foreign key (education_group_id)
            references education_group (id);

alter table module_tests
    add constraint FKhcs5fifpolhomautn8l4j8d9m
        foreign key (tests_id)
            references test (id);

alter table module_tests
    add constraint FKbt6dyr1qbp4tqh5r03lj0rafx
        foreign key (module_id)
            references module (id);

alter table question_answers_options
    add constraint FKtb85w4dhw0wa5rax226aluq00
        foreign key (question_id)
            references question (id);

alter table question_correct_answer
    add constraint FKhmjosex8na81iwaabontnmwbf
        foreign key (question_id)
            references question (id);

alter table student_all_marks
    add constraint FK4gr9cl2p4ylmbf2uu3wk4t24h
        foreign key (student_id)
            references student (id);

alter table student_groups
    add constraint FKkgbbdtbqdnny43mct3t9qk4v6
        foreign key (groups_id)
            references education_group (id);

alter table student_groups
    add constraint FKojsj5rrj0bn2n461wv97h7jon
        foreign key (student_id)
            references student (id);

alter table teacher_groups
    add constraint FK7f2nutnc7k9yi2ao0sylq8v93
        foreign key (groups_id)
            references education_group (id);

alter table teacher_groups
    add constraint FKh6egnyvnn8toiohupap6fmhu9
        foreign key (teacher_id)
            references teacher (id);

alter table test
    add constraint FKrr4kbnllq7skxok3wcy8ibgxk
        foreign key (module_id)
            references module (id);

alter table test_grading_system
    add constraint FK1l0iuuc3soigobg0cve7at85r
        foreign key (test_id)
            references test (id);

alter table test_questions
    add constraint FKramd83xrqk5s2hwob9sj2m8ne
        foreign key (questions_id)
            references question (id);

alter table test_questions
    add constraint FKjyd3d00gosup8x9q5ojnhop0q
        foreign key (test_id)
            references test (id);

alter table test_students_marks
    add constraint FK5f8hmblsrtnqax7r3w65hccvi
        foreign key (test_id)
            references test (id);

alter table students_answers
    add constraint FKkuolqw2rtl9p3bsq89gmmax9c
        foreign key (question_id)
            references question (id);

alter table students_answers
    add constraint FK3su2m0nh1961re1guhcpyqr36
        foreign key (student_id)
            references student (id);

alter table students_answers_students_answers
    add constraint FKkjlrlsivv4nvryg1t6a0he6m7
        foreign key (students_answers_id)
            references students_answers (id);

alter table question_students_answers
    add constraint FKh9tffdi0c35ajefn9v5ppne5g
        foreign key (students_answers_id)
            references students_answers (id);

alter table question_students_answers
    add constraint FKbmkl8t8hyyy0jo3rkx1nl95q
        foreign key (question_id)
            references question (id);

alter table module_control_work
    add constraint FK3gwyx9rpd6eox2dnacfw374ua
        foreign key (module_id)
            references module (id);
