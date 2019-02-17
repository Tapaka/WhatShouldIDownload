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

    /**
     * Initialize all the variables we're going to need.
     */
    private static void initializeGlobalValues(){
        foundSplitRequirement = false;
        episodeRegex = false;
        seasonEpisode = "";
        seasonSplit = 0;
        season = 1;
        episode = 0;
    }

    /**
     * A main method
     * @param args args
     */
    public static void main(String[] args) {
        File folder = new File("F:\\TVShows");
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            goDeeper(listOfFiles[i]);
        }

        displayTvShows();
    }

    /**
     * Process a file if it's a file and go deeper if it's a folder
     * @param file the file being processed
     */
    private static void goDeeper(File file){
        if (file.isFile()) {
            processFile(file);
        }else if (file.isDirectory()) {
            for(int i = 0; i < file.listFiles().length; i++){
                goDeeper(file.listFiles()[i]);
            }
        }
    }

    /**
     * Prints out the list of shows with their matching current season/episode on the drive
     */
    private static void displayTvShows(){
        Stream<Entry<String, Integer[]>> mapSorted = mapFileSeason.entrySet().stream().sorted(Entry.comparingByKey());
        mapSorted.forEach(x -> System.out.println(x.getKey() + " : S"+addZeros(x.getValue()[0])+"E"+addZeros(x.getValue()[1])));
    }


    /**
     * Determine which regex should be used to get the season episode number pair.
     * @param splitString the splitted string containing the episode season combo and other stuff
     * @param currentIndex the current index, so that we can know which element of the array contains the season episode combo
     */
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

    /**
     * Initialize all the global variables required, determine which split should be used (dot or space), determine which split should be used to get the season episode number combo
     * get the showname, the episode season number combo from the regex and fill the map
     * @param file the file currently being processed
     */
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

        for(int i = 0; i < splitString.length; i++){
            if(!foundSplitRequirement) {
                getSplitRegex(splitString[i], i);
            }else{
                break;
            }
        }

        String showName = getShowName(splitString);
        getSeasonEpisode();

        setMap(episode, season, showName);
    }

    /**
     * Retrieve the season and episode from a specified string.
     */
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

    /**
     * Fill the map with a showname and an association of a season number and an episode number
     * @param episode the episode number of the file currently being processed
     * @param season the season number of the file currently being processed
     * @param showName the name of the show from the file currently being processed
     */
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

    /**
     * Retrieve the showname from an array of string
     * @param splitString the array of string containing the showname among other things
     * @return the showname
     */
    private static String getShowName(String[] splitString){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < seasonSplit; i++){
            if(!splitString[i].matches("([0-9]{4})")) {
                sb.append(splitString[i]);
                if (i < seasonSplit - 1) {
                    sb.append(" ");
                }
            }
        }
        return sb.toString();
    }

    /**
     * Add a zero to display something like S01 or E01 instead of S1 or E1 since episode/season number are integers.
     * @param i the season number or episode number on which to add a zero
     * @return the season number or episode number with format "0X" if X < 10
     */
    private static String addZeros(Integer i){
        StringBuilder sb = new StringBuilder();
        if(String.valueOf(i).length() < 2){
            sb.append("0");
        }
        sb.append(i);
        return sb.toString();
    }
}
