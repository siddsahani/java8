import com.sun.org.apache.xpath.internal.operations.Bool;

import javax.swing.text.html.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class Main {

    enum CaloricLevel { DIET, NORMAL, FAT}

    private static final int CODEC_BUFFER_SIZE_BYTES = 16;
    // Using a Base64 Url and filename safe encoder
    private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder();
    private static final Pattern BASE64_SUFFIX_PADDING_REGEX = Pattern.compile("=*$");

    private static final int MAX_PREFIX_LENGTH = 5;
    private static final int MAX_SUFFIX_LENGTH = 5;
    private static final int MAX_SKU_LENGTH = 40;

    public static void main(String[] args) {
//        dishesExample();
//        tradingExample();
//        reducingExample();
        generateSKU();

        Stream.iterate(new int[]{0, 1}, i -> new int[] {i[1], i[0] + i [1]})
                .limit(10)
                .map(t -> t[0])
                .forEach(System.out::println);
    }

    private static void generateSKU() {
        UUID uuid = java.util.UUID.randomUUID();
        System.out.println("UUID : " + uuid.toString());
        ByteBuffer buffer = ByteBuffer.wrap(new byte[CODEC_BUFFER_SIZE_BYTES]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        String base64EncodedString = BASE64_ENCODER.encodeToString(buffer.array());
//        System.out.println("Encoded String Pre clean Up " + base64EncodedString);
        String encodedString = removeBase64Padding(base64EncodedString);
//        System.out.println("Encoded String Post clean Up " + encodedString);
        String generatedSku = "MAS-" + encodedString;
        generatedSku = generatedSku + "";
        System.out.println("Generated SKU " + generatedSku);
    }

    private static String removeBase64Padding(final String encodedValue) {
        return BASE64_SUFFIX_PADDING_REGEX.matcher(encodedValue).replaceFirst("");
    }

    private static void reducingExample() {

        Trader raoul = new Trader("Raoul", "Cambridge");
        Trader mario = new Trader("Mario","Milan");
        Trader alan = new Trader("Alan","Cambridge");
        Trader brian = new Trader("Brian","Cambridge");

        List<Transaction> transactions = Arrays.asList(
                new Transaction(brian, 2011, 300),
                new Transaction(raoul, 2012, 1000),
                new Transaction(raoul, 2011, 400),
                new Transaction(mario, 2012, 710),
                new Transaction(mario, 2012, 700),
                new Transaction(alan, 2012, 950)
        );

        // MAP all the transaction based on Year
        Map<Integer, List<Transaction>> map = transactions.stream()
                .collect(groupingBy(Transaction::getYear));

//        map.forEach((key, value) -> {System.out.println(key); System.out.println(value);});


        String menu = Dish.menu.stream().map(Dish::getName).collect(reducing((d1, d2) -> d1 + " " + d2)).get();
        System.out.println(menu);


        // grouping based on additional attributes as key
        Map<CaloricLevel, List<Dish>> dishesByCaloriesLevel = Dish.menu.stream()
                .collect(groupingBy(dish -> {
                    if (dish.getCalories() <= 400) return CaloricLevel.DIET;
                    else if (dish.getCalories() <= 700) return CaloricLevel.NORMAL;
                    else return CaloricLevel.FAT;
                }));
        System.out.println(dishesByCaloriesLevel);

        // MultiLevel grouping
        Map<Dish.Type, Map<CaloricLevel, List<Dish>>> dishByTypeByCaloriLevel = Dish.menu.stream()
                .collect(groupingBy(Dish::getType, groupingBy(
                        dish -> {
                            if (dish.getCalories() <= 400) return CaloricLevel.DIET;
                            else if (dish.getCalories() <= 700) return CaloricLevel.NORMAL;
                            else return CaloricLevel.FAT;
                        })
                ));
        System.out.println(dishByTypeByCaloriLevel);

        // Another way of multilevel grouping
        Map<Dish.Type, Set<CaloricLevel>> dishByTypeByCaloriLevel2 = Dish.menu.stream()
                .collect(groupingBy(Dish::getType, mapping(
                            dish -> {
                                if (dish.getCalories() <= 400) return CaloricLevel.DIET;
                                else if (dish.getCalories() <= 700) return CaloricLevel.NORMAL;
                                else return CaloricLevel.FAT;
                             }, toSet())));
        System.out.println(dishByTypeByCaloriLevel2);


        // Partitioning
        Map<Boolean, List<Dish>> vegDishPartition = Dish.menu.stream()
                .collect(partitioningBy(Dish::isVegetarian));
        System.out.println(vegDishPartition);

        // Multilevel Partitioning
        Map<Boolean, Map<Dish.Type, List<Dish>>> multiPartition = Dish.menu.stream()
                .collect(partitioningBy(Dish::isVegetarian, groupingBy(Dish::getType)));
        System.out.println(multiPartition);

    }
    private static void tradingExample() {
        Trader raoul = new Trader("Raoul", "Cambridge");
        Trader mario = new Trader("Mario","Milan");
        Trader alan = new Trader("Alan","Cambridge");
        Trader brian = new Trader("Brian","Cambridge");

        List<Transaction> transactions = Arrays.asList(
                new Transaction(brian, 2011, 300),
                new Transaction(raoul, 2012, 1000),
                new Transaction(raoul, 2011, 400),
                new Transaction(mario, 2012, 710),
                new Transaction(mario, 2012, 700),
                new Transaction(alan, 2012, 950)
        );

        System.out.println("1");
        transactions.stream()
                .filter(p -> p.getYear() == 2011)
                .sorted(Comparator.comparing(Transaction::getValue))
                .forEach(System.out::println);

        System.out.println("2");
        transactions.stream()
                .map(p -> p.getTrader().getCity())
                .distinct()
                .forEach(System.out::println);

        System.out.println("3");
        transactions.stream()
                .map(t -> t.getTrader())
                .filter(p -> "Cambridge".equals(p.getCity()))
                .sorted(Comparator.comparing(p -> p.getName()))
                .distinct()
                .forEach(System.out::println);

        System.out.println("4");
        System.out.println(transactions.stream()
                .map(t -> t.getTrader())
                .distinct()
                .sorted(Comparator.comparing(p -> p.getName()))
                .map(p -> p.getName())
                .reduce("", (a,b) -> String.format("%s , %s", a, b)));

        System.out.println("5");
        boolean flag = transactions.stream()
                .map(p -> p.getTrader().getCity())
                .anyMatch(p -> "Milan".equals(p));
        System.out.println("result : " + flag);

        System.out.println("6");
        transactions.stream()
                .filter(p -> "Cambridge".equals(p.getTrader().getCity()))
                .forEach(p -> System.out.println(p.getValue()));

        System.out.println("7");
        System.out.println("Highest Value : " + transactions.stream()
                .map(p -> p.getValue())
                .reduce(-1, Integer::max));
    }

    private static void dishesExample() {
        /**
         * Simple Filter
         */
        List<Dish> vegDishes = Dish.menu.stream()
                .filter(Dish::isVegetarian)
                .collect(toList());
        vegDishes.stream().forEach(System.out::println);



        List<Integer> numberList = Arrays.asList(1,2,1,3,3,2,4);
        numberList.stream()
                .filter(i -> i % 2 == 0)
                .distinct()
                .forEach(System.out::println);

        /**
         * Filter to skip the first 2 dish having more 300 calories and skip the rest
         */
        List<Dish> highCaloriesDish = Dish.menu
                .stream()
                .filter(c -> c.getCalories() > 300)
                .skip(2)
                .collect(toList());
        highCaloriesDish.stream().forEach(System.out::println);

        /**
         * Filter first 2 meat dishes
         */
        Dish.menu.stream()
                .filter(m -> m.getType() == Dish.Type.MEAT)
                .limit(2)
                .forEach(System.out::println);

        /**
         * Map Example
         */
        Dish.menu.stream()
                .map(Dish::getType)
                .forEach(System.out::println);

        /**
         * Given the list of words, print the number of characters in each word.
         */
        List<String> words = Arrays.asList("Java8", "in", "Action", "Lambda", "Streams");
        words.stream()
                .map(String::length)
                .forEach(System.out::println);

        /**
         * Return list of unique elements from all the given words
         */
        words.stream()
                .map(array -> array.split(""))
                .flatMap(Arrays::stream)
                .distinct()
                .collect(toList())
                .forEach(System.out::println);

        System.out.println("Iterate 2 list");
        List<Integer> i1 = Arrays.asList(1,2,3);
        List<Integer> i2 = Arrays.asList(3, 4);
        i1.stream()
            .flatMap(i -> i2.stream()
                    .map(val -> Arrays.asList(i, val)))
                .forEach(System.out::println);

        System.out.println("Quiz 5.2 3");
        i1.stream()
                .flatMap(i -> i2.stream()
                        .filter(val -> (i+val)%3 == 0)
                        .map(val -> Arrays.asList(i, val)))
                .forEach(System.out::println);

        System.out.println("Reduce");
        List<Integer> list = Arrays.asList(1,2,3,4,5,6,0,13,-1);
        int sum = list.stream().reduce(0, Integer::sum);
        System.out.println("sum : " + sum);

        Optional<Integer> max = list.stream()
                //.reduce((a, b) -> a >=b ? a : b);
                  .reduce(Integer::max);
        System.out.println("max : " + max.orElse(-100));

        Optional<Integer> min = list.stream()
                //.reduce((a, b) -> a >=b ? b : a);
                .reduce(Integer::min);
        System.out.println("min : " + min.orElse(-100));

        System.out.println("Quiz 5.3");
        int count = Dish.menu.stream()
                .map(d -> 1)
                .reduce(0, Integer::sum);
        System.out.println("Number of dishes : " + count);
    }


}
