package com.ftn.sbnz.model.enums;

public enum LesionType {
    NONE(0),
    COMEDONES(1),
    PAPULES(2),
    PUSTULES(3),
    NODULES(4);

    private final int grade;

    LesionType(int grade) {
        this.grade = grade;
    }

    public int getGrade() {
        return grade;
    }
}
