import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Main {
    static Map<String, Integer[]> mapFileSeason = new HashMap<>();
    public static void main(String[] args) {
        File folder = new File("F:\\TVShows");
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            goDeeper(listOfFiles[i]);
        }

        Stream<Entry<String, Integer[]>> mapSorted = mapFileSeason.entrySet().stream().sorted(Entry.comparingByKey());
        mapSorted.forEach(x -> System.out.println(x.getKey() + " : S"+addZeros(x.getValue()[0])+"E"+addZeros(x.getValue()[1])));

//        for(Entry<String, Integer[]> entry : mapFileSeason.entrySet()){
////            System.out.println(entry.getKey() + " : S"+addZeros(entry.getValue()[0])+"E"+addZeros(entry.getValue()[1]));
////        }
    }

    private static void goDeeper(File file){
        if (file.isFile()) {
            processFile(file);
        }else if (file.isDirectory()) {
            for(int i = 0; i < file.listFiles().length; i++){
                goDeeper(file.listFiles()[i]);
            }
        }
    }

    private static void processFile(File file){
        String[] splitString = file.getName().split("\\.");
        Integer seasonSplit = 0;
        String seasonEpisode = "";
        for(int k = 0; k < splitString.length; k++){
            if(splitString[k].matches("(S[0-9]{2}E[0-9]{2})")){
                seasonEpisode = splitString[k];
                seasonSplit = k;
                break;
            }
        }
        StringBuilder sb = new StringBuilder();
        for(int l = 0; l < seasonSplit; l++){
            if(!splitString[l].matches("([0-9]{4})")) {
                sb.append(splitString[l]);
                if (l < seasonSplit - 1) {
                    sb.append(" ");
                }
            }
        }
        String showName = sb.toString();

        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(seasonEpisode);
        Integer season = 0, episode = 0;
        while(m.find()) {
            if (season == 0) {
                season = Integer.valueOf(m.group());
            } else {
                episode = Integer.valueOf(m.group());
            }
        }

        if(!mapFileSeason.containsKey(showName.toLowerCase())){
            mapFileSeason.put(sb.toString().toLowerCase(), new Integer[]{season, episode});
        }else {
            Integer[] latestSeasonEpisode = mapFileSeason.get(showName.toLowerCase());
            if (season >= latestSeasonEpisode[0] && episode > latestSeasonEpisode[1]) {
                mapFileSeason.put(showName.toLowerCase(), new Integer[]{season, episode});
            }
        }
    }

    private static String addZeros(Integer i){
        StringBuilder sb = new StringBuilder();
        if(String.valueOf(i).length() < 2){
            sb.append("0");
        }
        sb.append(String.valueOf(i));
        return sb.toString();
    }
}
