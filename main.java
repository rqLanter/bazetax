import org.w3c.dom.*; // импортируем классы для работы с DOM (Document Object Model)
import javax.xml.parsers.*; // импортируем классы для парсинга XML
import java.io.*; //импортируем классы для ввода/вывода
import java.nio.file.*; //импортируем классы для работы с файлами и путями
import java.util.*; //импортируем коллекции, такие как Map и другие утилиты

public class TaxA {
    public static void main(String[] args) {
        Path folderPath = Paths.get(""); // УКАЖИТЕ ПУТЬ К ПАПКЕ С ФАЙЛАМИ
        Map<String, Double> regionTaxSum = new HashMap<>(); // Карта для хранения сумм налогов по регионам (ключ - код региона, значение - сумма)

        try {
            // Чтение всех файлов с нужным расширением и именем
            Files.walk(folderPath) //создаёт поток (Stream<Path>), который позволяет обходить директорию и её подкаталоги.
                    .filter(Files::isRegularFile) // только обычные файлы. Фильтрует поток, оставляя только обычные файлы (не папки, не символические ссылки).
                    .filter(file -> file.toString().endsWith(".xml") && file.getFileName().toString().startsWith("testLearn")) // Фильтруем по маске //toStreang метод, который возвращает строковое представление объекта//startsWith() в Java — это метод, который проверяет, начинается ли строка с указанного префикса.//-> — лямбда-оператор, который указывает, что filePath будет передан в тело функции
                    .forEach(filePath -> { //для каждого файла, соответствующего фильтрам
                        try {
                            parseXMLFile(filePath, regionTaxSum); //вызываем метод для парсинга XML-файла и обновления карты с налогами
                        } catch (Exception e) { //обрабатываем исключения, возникшие при парсинге
                            System.err.printf("Ошибка при обработке файла %s: %s%n", filePath, e.getMessage()); //выводим сообщение об ошибке
                        }
                    });

            //суммируем налоги по всем регионам
            double totalTaxSum = regionTaxSum.values().stream().mapToDouble(Double::doubleValue).sum(); //суммируем все значения в карте regionTaxSum

            //выводим результаты
            regionTaxSum.forEach((regionCode, regionTax) -> { //для каждого региона в карте
                double percentage = totalTaxSum > 0 ? (regionTax / totalTaxSum) * 100 : 0; //рассчитываем процент от общей суммы налогов
                System.out.printf("Код региона: %s, Сумма уплаченного налога: %.2f, Процент от общей суммы налогов: %.2f%%\n",
                        regionCode, regionTax, percentage); //выводим код региона, сумму налога и процент
            });

        } catch (IOException e) { //обрабатываем исключения, связанные с файловой системой
            System.err.println("Ошибка при чтении файлов: " + e.getMessage()); //выводим сообщение об ошибке
        }
    }

    //Метод для парсинга XML-файла, суммирования налогов по регионам
    private static void parseXMLFile(Path filePath, Map<String, Double> regionTaxSum) throws Exception {
        //создание экземпляра для создания парсера XML-документов
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); //создаем фабрику для парсинга XML
        DocumentBuilder builder = factory.newDocumentBuilder(); //создаем парсер (DocumentBuilder) для обработки XML
        //парсинг XML-файла и создание объекта документа
        Document doc = builder.parse(filePath.toFile()); //парсим XML-файл и получаем объект Document

        //Получение всех элементов <Item> из документа
        NodeList items = doc.getElementsByTagName("Item"); //получаем список всех узлов <Item> в XML-документе
        //Перебор всех найденных элементов <Item>
        for (int i = 0; i < items.getLength(); i++) { //цикл по всем элементам <Item>
            //Получение текущего элемента <Item>
            Node itemNode = items.item(i); //берем текущий узел из списка
            //Проверка, является ли узел элементом а не текстом или комментарием
            if (itemNode.getNodeType() == Node.ELEMENT_NODE) { //проверяем, является ли узел элементом
                Element itemElement = (Element) itemNode; //приводим узел к типу Element
                String oktmo = itemElement.getElementsByTagName("OKTMO").item(0).getTextContent(); //извлекаем текст из элемента <OKTMO>
                String taxSumStr = itemElement.getElementsByTagName("TaxSum").item(0).getTextContent(); //извлекаем текст из элемента <TaxSum>
                // Преобразование строки с суммой налога в число с плавающей точкой
                double taxSum = Double.parseDouble(taxSumStr); // Преобразуем строку с суммой налога в double

                        //берем первые 8 цифр кода ОКТМО
                String regionCode = oktmo.length() >= 8 ? oktmo.substring(0, 8) : oktmo; // Если длина >= 8, берем первые 8 символов, иначе весь код

                // Группируем налоги по регионам
                regionTaxSum.merge(regionCode, taxSum, Double::sum); // Если регион уже есть, добавляем сумму, иначе создаем новую запись
            }
        }
    }
}

