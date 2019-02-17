import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Main {
    private static Map<String, Integer[]> mapFileSeason;
    private static Boolean foundSplitRequirement, episodeRegex;
    private static String seasonEpisode;
    private static Integer seasonSplit, season, episode;
    private final static String[] regexList;

    static{
        mapFileSeason = new HashMap<>();
        regexList = new String[]{"(S[0-9]{2}E[0-9]{2})", "(Episode|episode)", "(S[0-9]{2}\\.E[0-9]{2})"};
        initializeGlobalValues();
    }

    private static void initializeGlobalValues(){
        foundSplitRequirement = false;
        episodeRegex = false;
        seasonEpisode = "";
        seasonSplit = 0;
        season = 1;
        episode = 0;
    }
    public static void main(String[] args) {
        File folder = new File("F:\\TVShows");
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            goDeeper(listOfFiles[i]);
        }

        displayTvShows();
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

    private static void displayTvShows(){
        Stream<Entry<String, Integer[]>> mapSorted = mapFileSeason.entrySet().stream().sorted(Entry.comparingByKey());
        mapSorted.forEach(x -> System.out.println(x.getKey() + " : S"+addZeros(x.getValue()[0])+"E"+addZeros(x.getValue()[1])));
    }



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

    private static void processFile(File file){
        initializeGlobalValues();
        String[] splitOnDot = file.getName().split("\\.");
        String[] splitOnSpace = file.getName().split("\\s");
        String[] splitString = null;
        if(splitOnDot.length < splitOnSpace.length){
            splitString = splitOnSpace;
        }else if(splitOnDot.length > splitOnSpace.length){
            splitString = splitOnDot;
        }else if(splitOnDot.length == splitOnSpace.length){
            //Cas "Serie S01E01.mp4"
            splitString = file.getName().substring(0, file.getName().indexOf('.')).split("\\s");
        }

        for(int k = 0; k < splitString.length; k++){
            if(!foundSplitRequirement) {
                getSplitRegex(splitString[k], k);
            }else{
                break;
            }
        }

        String showName = getShowName(splitString);
        getSeasonEpisode();

        setMap(episode, season, showName);
    }

    private static void getSeasonEpisode(){
        Boolean setSeason = false;
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(seasonEpisode);
        while(m.find()) {
            if (!setSeason && season == 1 && (seasonEpisode.matches(regexList[0]) || seasonEpisode.matches(regexList[2]))) {
                setSeason = true;
                season = Integer.valueOf(m.group());
            } else {
                episode = Integer.valueOf(m.group());
            }
        }
    }

    private static void setMap(Integer episode, Integer season, String showName){
        if(episode != 0){
            if(!mapFileSeason.containsKey(showName)){
                mapFileSeason.put(showName.toLowerCase(), new Integer[]{season, episode});
            }else {
                Integer[] latestSeasonEpisode = mapFileSeason.get(showName);
                if (season >= latestSeasonEpisode[0] && episode > latestSeasonEpisode[1]) {
                    mapFileSeason.put(showName, new Integer[]{season, episode});
                }
            }
        }
    }

    private static String getShowName(String[] splitString){
        StringBuilder sb = new StringBuilder();
        for(int l = 0; l < seasonSplit; l++){
            if(!splitString[l].matches("([0-9]{4})")) {
                sb.append(splitString[l]);
                if (l < seasonSplit - 1) {
                    sb.append(" ");
                }
            }
        }
        return sb.toString();
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
