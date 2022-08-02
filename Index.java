import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Stream;
// Название класса не говорит ни о чем, мб стоит дать название получше?
public class Index {
    TreeMap<String, List<Pointer>> invertedIndex;

    ExecutorService pool;
// В строках 9-10 не указаны модификаторы доступа, т.е любой другой класс из пакета может модифицировать поля
// (что ломает инкапсуляцию)
    public Index(ExecutorService pool) {
        this.pool = pool;
        invertedIndex = new TreeMap<>(); //лучше использовать ключевое слово this
    }

    public void indexAllTxtInPath(String pathToDir) throws IOException {
        Path of = Path.of(pathToDir); //очень странные названия у переменных, не особо понятно, что в ней должно лежать

        BlockingQueue<Path> files = new ArrayBlockingQueue<>(2); // а почему 2? Magic number

        try (Stream<Path> stream = Files.list(of)) {
            stream.forEach(files::add);
        }
// китайский код, я бы тут возможно использовал цикл (+а почему три раза?)
        pool.submit(new IndexTask(files));
        pool.submit(new IndexTask(files));
        pool.submit(new IndexTask(files));
    }

    public TreeMap<String, List<Pointer>> getInvertedIndex() {
        return invertedIndex; //такой возврат коллекции возвращает ссылку, т.е получатель может изменить коллекцию
    }
    //                   \/ нарушение кодстайла, метод должен начинаться с маленькой буквы, +аналогично строке 34
    public List<Pointer> GetRelevantDocuments(String term) {
        return invertedIndex.get(term);
    }

    public Optional<Pointer> getMostRelevantDocument(String term) {
        return invertedIndex.get(term).stream().max(Comparator.comparing(o -> o.count));
        //можно сделать читаемее, если каждый метод начинать на отдельной строке (кодстайл)
    }
    static class Pointer {
        private Integer count;
        private String filePath;

        public Pointer(Integer count, String filePath) {
            this.count = count;
            this.filePath = filePath;
        }

        @Override
        public String toString() {
            return "{" + "count=" + count + ", filePath='" + filePath + '\'' + '}';
        }
    }
//слишком много функционала внутри одного класса, мб лучше разбить его на отдельные классы?
    class IndexTask implements Runnable {

        private final BlockingQueue<Path> queue;

        public IndexTask(BlockingQueue<Path> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            try {
                Path take = queue.take(); // тоже не очень понятное название переменной
                List<String> strings = Files.readAllLines(take);
//              тут бы тоже не помешал перенос методов построчно, чтобы было проще читать
                strings.stream().flatMap(str -> Stream.of(str.split(" "))).forEach(word -> invertedIndex.compute(word, (k, v) -> {
                    if (v == null) return List.of(new Pointer(1, take.toString())); //вынести return на отдельную строку
                    else {
                        ArrayList<Pointer> pointers = new ArrayList<>();

                        if (v.stream().noneMatch(pointer -> pointer.filePath.equals(take.toString()))) {
                            pointers.add(new Pointer(1, take.toString()));
                        }
                        //мб лучше использовать filter вместо if? Все равно стримы используем
                        v.forEach(pointer -> {
                            if (pointer.filePath.equals(take.toString())) {
                                pointer.count = pointer.count + 1;
                                //а тут можно сделать инкрементацию pointer.count++;
                            }
                        });

                        pointers.addAll(v);

                        return pointers;
                    }

                }));

            } catch (InterruptedException | IOException e) {
                throw new RuntimeException();
                //бросаем рантайм экспепшн, причем вообще непонятно, а если он упадет еще где-то?
                //лучше хотя бы передавать в него текст ошибки, чтобы клиенту было понятно хотя бы что-то
                //а лучше пользоваться кастомными экспепшнами
            }
        }
    }
}