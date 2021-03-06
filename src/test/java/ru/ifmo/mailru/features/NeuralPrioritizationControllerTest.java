package ru.ifmo.mailru.features;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author Anastasia Lebedeva
 */
public class NeuralPrioritizationControllerTest {

    @Test
    public void mainTest() {
        try {
            List<Double> realValues = new ArrayList<>();
            List<Double> expectedValue = new ArrayList<>();
            NeuralPrioritizationController controller = new NeuralPrioritizationController();
            int pos = controller.positiveExamples.size();
            int neg = controller.negativeExamples.size();
            System.out.println(pos + " " + neg);
            controller.train(pos, neg);
            Scanner scanner = new Scanner(new File(NeuralPrioritizationController.RANKS_FILE));
            int n = 0;
            double mean = 0;
            while (scanner.hasNext()) {
                String s = scanner.nextLine();
                String[] arr = s.split(" ");
                try {
                    double real = controller.computeRank(arr[0]) * 11 - 1;
                    double expected = Double.valueOf(arr[1]);
                    realValues.add(real);
                    realValues.add(expected);
                    if (expected > -2) {
                        mean += Math.abs(real - expected);
                        //mean += (real - expected) * (real - expected);
                        n++;
                    }
                    System.out.println("real: " + real + " expected: " + expected);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            //System.out.println(Math.sqrt(mean) / n);
            System.out.println(mean / n);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
