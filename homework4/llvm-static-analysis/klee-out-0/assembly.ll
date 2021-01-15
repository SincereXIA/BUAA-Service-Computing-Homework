; ModuleID = 'get_sign.bc'
source_filename = "get_sign.c"
target datalayout = "e-m:e-i64:64-f80:128-n8:16:32:64-S128"
target triple = "x86_64-pc-linux-gnu"

@.str = private unnamed_addr constant [2 x i8] c"a\00", align 1

; Function Attrs: noinline nounwind uwtable
define dso_local i32 @get_sign(i32) #0 !dbg !7 {
  %2 = alloca i32, align 4
  %3 = alloca i32, align 4
  store i32 %0, i32* %3, align 4
  call void @llvm.dbg.declare(metadata i32* %3, metadata !11, metadata !DIExpression()), !dbg !12
  %4 = load i32, i32* %3, align 4, !dbg !13
  %5 = icmp eq i32 %4, 0, !dbg !15
  br i1 %5, label %6, label %7, !dbg !16

6:                                                ; preds = %1
  store i32 0, i32* %2, align 4, !dbg !17
  br label %12, !dbg !17

7:                                                ; preds = %1
  %8 = load i32, i32* %3, align 4, !dbg !18
  %9 = icmp slt i32 %8, 0, !dbg !20
  br i1 %9, label %10, label %11, !dbg !21

10:                                               ; preds = %7
  store i32 -1, i32* %2, align 4, !dbg !22
  br label %12, !dbg !22

11:                                               ; preds = %7
  store i32 1, i32* %2, align 4, !dbg !23
  br label %12, !dbg !23

12:                                               ; preds = %11, %10, %6
  %13 = load i32, i32* %2, align 4, !dbg !24
  ret i32 %13, !dbg !24
}

; Function Attrs: nounwind readnone speculatable
declare void @llvm.dbg.declare(metadata, metadata, metadata) #1

; Function Attrs: noinline nounwind uwtable
define dso_local i32 @main() #0 !dbg !25 {
  %1 = alloca i32, align 4
  %2 = alloca i32, align 4
  store i32 0, i32* %1, align 4
  call void @llvm.dbg.declare(metadata i32* %2, metadata !28, metadata !DIExpression()), !dbg !29
  %3 = call i32 (i32*, i64, i8*, ...) bitcast (i32 (...)* @klee_make_symbolic to i32 (i32*, i64, i8*, ...)*)(i32* %2, i64 4, i8* getelementptr inbounds ([2 x i8], [2 x i8]* @.str, i64 0, i64 0)), !dbg !30
  %4 = load i32, i32* %2, align 4, !dbg !31
  %5 = call i32 @get_sign(i32 %4), !dbg !32
  ret i32 %5, !dbg !33
}

declare dso_local i32 @klee_make_symbolic(...) #2

attributes #0 = { noinline nounwind uwtable "correctly-rounded-divide-sqrt-fp-math"="false" "disable-tail-calls"="false" "less-precise-fpmad"="false" "min-legal-vector-width"="0" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-jump-tables"="false" "no-nans-fp-math"="false" "no-signed-zeros-fp-math"="false" "no-trapping-math"="false" "stack-protector-buffer-size"="8" "target-cpu"="x86-64" "target-features"="+cx8,+fxsr,+mmx,+sse,+sse2,+x87" "unsafe-fp-math"="false" "use-soft-float"="false" }
attributes #1 = { nounwind readnone speculatable }
attributes #2 = { "correctly-rounded-divide-sqrt-fp-math"="false" "disable-tail-calls"="false" "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "no-signed-zeros-fp-math"="false" "no-trapping-math"="false" "stack-protector-buffer-size"="8" "target-cpu"="x86-64" "target-features"="+cx8,+fxsr,+mmx,+sse,+sse2,+x87" "unsafe-fp-math"="false" "use-soft-float"="false" }

!llvm.dbg.cu = !{!0}
!llvm.module.flags = !{!3, !4, !5}
!llvm.ident = !{!6}

!0 = distinct !DICompileUnit(language: DW_LANG_C99, file: !1, producer: "clang version 9.0.0-2~ubuntu18.04.2 (tags/RELEASE_900/final)", isOptimized: false, runtimeVersion: 0, emissionKind: FullDebug, enums: !2, nameTableKind: None)
!1 = !DIFile(filename: "get_sign.c", directory: "/home/zhangjh/workspace/BUAA-Service-Computing-Homework/homework4/llvm-static-analysis")
!2 = !{}
!3 = !{i32 2, !"Dwarf Version", i32 4}
!4 = !{i32 2, !"Debug Info Version", i32 3}
!5 = !{i32 1, !"wchar_size", i32 4}
!6 = !{!"clang version 9.0.0-2~ubuntu18.04.2 (tags/RELEASE_900/final)"}
!7 = distinct !DISubprogram(name: "get_sign", scope: !1, file: !1, line: 1, type: !8, scopeLine: 1, flags: DIFlagPrototyped, spFlags: DISPFlagDefinition, unit: !0, retainedNodes: !2)
!8 = !DISubroutineType(types: !9)
!9 = !{!10, !10}
!10 = !DIBasicType(name: "int", size: 32, encoding: DW_ATE_signed)
!11 = !DILocalVariable(name: "x", arg: 1, scope: !7, file: !1, line: 1, type: !10)
!12 = !DILocation(line: 1, column: 18, scope: !7)
!13 = !DILocation(line: 2, column: 7, scope: !14)
!14 = distinct !DILexicalBlock(scope: !7, file: !1, line: 2, column: 7)
!15 = !DILocation(line: 2, column: 9, scope: !14)
!16 = !DILocation(line: 2, column: 7, scope: !7)
!17 = !DILocation(line: 3, column: 6, scope: !14)
!18 = !DILocation(line: 5, column: 7, scope: !19)
!19 = distinct !DILexicalBlock(scope: !7, file: !1, line: 5, column: 7)
!20 = !DILocation(line: 5, column: 9, scope: !19)
!21 = !DILocation(line: 5, column: 7, scope: !7)
!22 = !DILocation(line: 6, column: 6, scope: !19)
!23 = !DILocation(line: 8, column: 6, scope: !19)
!24 = !DILocation(line: 9, column: 1, scope: !7)
!25 = distinct !DISubprogram(name: "main", scope: !1, file: !1, line: 11, type: !26, scopeLine: 11, spFlags: DISPFlagDefinition, unit: !0, retainedNodes: !2)
!26 = !DISubroutineType(types: !27)
!27 = !{!10}
!28 = !DILocalVariable(name: "a", scope: !25, file: !1, line: 12, type: !10)
!29 = !DILocation(line: 12, column: 7, scope: !25)
!30 = !DILocation(line: 13, column: 3, scope: !25)
!31 = !DILocation(line: 14, column: 19, scope: !25)
!32 = !DILocation(line: 14, column: 10, scope: !25)
!33 = !DILocation(line: 14, column: 3, scope: !25)
