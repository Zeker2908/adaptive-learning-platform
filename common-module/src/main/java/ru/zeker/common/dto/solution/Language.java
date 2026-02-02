package ru.zeker.common.dto.solution;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Language {
    ASSEMBLY_NASM(45),
    BASH_5_0_0(46),
    BASIC_FBC_1_07_1(47),
    C_CLANG_7_0_1(75),
    CPP_CLANG_7_0_1(76),
    C_GCC_7_4_0(48),
    CPP_GCC_7_4_0(52),
    C_GCC_8_3_0(49),
    CPP_GCC_8_3_0(53),
    C_GCC_9_2_0(50),
    CPP_GCC_9_2_0(54),
    CLOJURE_1_10_1(86),
    C_SHARP_MONO_6_6_0_161(51),
    COBOL_GNOCOBOL_2_2(77),
    COMMON_LISP_SBCL_2_0_0(55),
    D_DMD_2_089_1(56),
    ELIXIR_1_9_4(57),
    ERLANG_OTP_22_2(58),
    EXECUTABLE(44),
    F_SHARP_NET_CORE_3_1_202(87),
    FORTRAN_GFORTRAN_9_2_0(59),
    GO_1_13_5(60),
    GROOVY_3_0_3(88),
    HASKELL_GHC_8_8_1(61),
    JAVA_OPENJDK_13_0_1(62),
    JAVASCRIPT_NODEJS_12_14_0(63),
    KOTLIN_1_3_70(78),
    LUA_5_3_5(64),
    MULTI_FILE_PROGRAM(89),
    OBJECTIVE_C_CLANG_7_0_1(79),
    OCAML_4_09_0(65),
    OCTAVE_5_1_0(66),
    PASCAL_FPC_3_0_4(67),
    PERL_5_28_1(85),
    PHP_7_4_1(68),
    PLAIN_TEXT(43),
    PROLOG_GNU_PROLOG_1_4_5(69),
    PYTHON_2_7_17(70),
    PYTHON_3_8_1(71),
    R_4_0_0(80),
    RUBY_2_7_0(72),
    RUST_1_40_0(73),
    SCALA_2_13_2(81),
    SQL_SQLITE_3_27_2(82),
    SWIFT_5_2_3(83),
    TYPESCRIPT_3_7_4(74),
    VISUAL_BASIC_NET_VBNC(84);

    private final int code;
}