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

    private static Boolean episodeRegex = false;
    private final static String[] regexList = new String[]{"(S[0-9]{2}E[0-9]{2})", "(Episode|episode)", "(S[0-9]{2}\\.E[0-9]{2})"};

    private static void getSplitRegex(String splitString, Integer currentIndex){
        if(episodeRegex){
            seasonEpisode = splitString;
            foundSplitRequirement = true;
        }else{
            for(String regex : regexList){
                if(splitString.matches(regex)){
                    if(regex.equals(regexList[1])){
                        episodeRegex = true;
                    }else{
                        seasonEpisode = splitString;
                        foundSplitRequirement = true;
                    }
                    seasonSplit = currentIndex;
                    break;
                }
            }
        }

    }

    private static Boolean foundSplitRequirement = false;
    private static String seasonEpisode = "";
    private static Integer seasonSplit = 0;

    private static void resetGlobalValues(){
        foundSplitRequirement = false;
        episodeRegex = false;
        seasonEpisode = "";
        seasonSplit = 0;
    }

    private static void processFile(File file){
        resetGlobalValues();
        String[] splitString = file.getName().split("\\.").length < 3 ? file.getName().split("\\s") : file.getName().split("\\.");
        if(file.getName().contains("code")){
            System.out.println("");
        }
        for(int k = 0; k < splitString.length; k++){
            if(!foundSplitRequirement) {
                getSplitRegex(splitString[k], k);
            }else{
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
        String showName = sb.toString().toLowerCase();
        if(showName.equals("code geass")){
            System.out.printf("");
        };
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(seasonEpisode);
        Integer season = 1, episode = 0;
        while(m.find()) {
            if (season == 0 && (seasonEpisode.matches(regexList[0]) || seasonEpisode.matches(regexList[2]))) {
                season = Integer.valueOf(m.group());
            } else {
                episode = Integer.valueOf(m.group());
            }
        }

        if(episode != 0){
            if(!mapFileSeason.containsKey(showName)){
                mapFileSeason.put(sb.toString().toLowerCase(), new Integer[]{season, episode});
            }else {
                Integer[] latestSeasonEpisode = mapFileSeason.get(showName);
                if (season >= latestSeasonEpisode[0] && episode > latestSeasonEpisode[1]) {
                    mapFileSeason.put(showName, new Integer[]{season, episode});
                }
            }
        }
    }

    private static String addZeros(Integer i){
        StringBuilder sb = new StringBuilder();
        if(String.valueOf(i).length() < 2){
            sb.append("0");
        }
        sb.append(i);
        return sb.toString();
    }
}
