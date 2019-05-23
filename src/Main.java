
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws IOException {
        ArrayList<String> inputs = new ArrayList<>(Arrays.asList("Inputfiles\\tvh_problem_3.txt",
                "Inputfiles\\tvh_problem_4.txt", "Inputfiles\\tvh_problem_6.txt", "Inputfiles\\tvh_problem_5.txt",
                "Inputfiles\\tvh_problem_7.txt", "Inputfiles\\tvh_problem_8.txt"));


        long t = System.currentTimeMillis();

        System.out.println(System.currentTimeMillis() - t);
        Problem p = new Problem(new File(args[0]));
        System.out.println(System.currentTimeMillis() - t);

        p.initialSolution();
        System.out.println(System.currentTimeMillis() - t);

        p.writeSolutionFile("");
        System.out.println(System.currentTimeMillis() - t);

        p.optimize(args.length >= 2 ? Integer.parseInt(args[1]) : 10);
        System.out.println(System.currentTimeMillis() - t);

        //p.calculateDistance(new int[]{0,131,0,197,177,225,108,55,144,1,259,109,2});

    }
}
/*
8 5265
5461
9027
 */
/*
7 5226
5552
9274

 */
/*
6 4891
5089
8866
8896
 */

/*
5 4749
4991
8832
4876
8595
 */
/*
4 3786
5231
11101
6000
 */

/*
3 1381
3188
995
 */