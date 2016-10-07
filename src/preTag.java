import org.ansj.domain.Term;
import org.ansj.library.UserDefineLibrary;
import org.ansj.splitWord.analysis.DicAnalysis;
import org.fnlp.nlp.cn.CNFactory;
import org.fnlp.util.exception.LoadModelException;

import java.io.*;
import java.util.*;

/**
 * 将医案中处方进行标记
 *
 * @author Qinfei Chen
 * @create 2016-09-27 10:38
 */
public class preTag {
    private static  Set<String> dic;
    private static  Set<String> dicPre;

    public static void main(String[] args) throws IOException, LoadModelException {
       /*读取中药词表和处方名词表*/
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("resource/med.txt")));
        BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream("resource/prescription.txt"),"GBK"));

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("resource/医案.csv"),"GBK"));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("out/医案pro8.csv"),"GBK"));
        dic = new HashSet<String>();
        dicPre = new HashSet<String>();
        String word;
        while((word = br.readLine()) != null) {
            if(word.trim().length() == 1 || word.trim().length() == 0){
                System.out.println(word);
                continue;
            }
            dic.add(word);
//            System.out.println(word);
        }
        String[] preFilter = {"汤","丸","散","饮","剂","灵","胶囊","片","膏","方","煎","汁"};
        while((word = br2.readLine()) != null) {
                    if(word.trim().length() == 1 || word.trim().length() == 0){
                        System.out.println(word);
                        continue;
                    }
                    for(String wordFileter: preFilter){
                        if(word.contains(wordFileter)){
                            word = word.split("（")[0];
                            dicPre.add(word);
                            continue;
                        }
            }
//            System.out.println(word);
        }
        //采用ansj分词，添加用户自定义词典
        initialDic(dicPre);
        initialDic(dic);

        br.close();
        br2.close();

        String record;
        while((record = reader.readLine()) != null){
            String[] part = record.split(",");
            String recordText = part[2].replace("|","。").replace("!","。").replace("？","。").replace("。。","。").replace("(","（").replace(")","）");
           //处理句子中含有括号的情况，暂不考虑嵌套的括号
            while(recordText.contains("（") && recordText.contains("）")){
                recordText = doBracket(recordText);
            }
            String[] sentence = recordText.split("。");
            String cfIndex = new String();
            for(int i = 0; i < sentence.length; i++){
//                if(fnlpTag(sentence[i],dic)){
                if(ansjTag(sentence[i])){
                    cfIndex = cfIndex + " " +(i+1);
                }
                sentence[i]  = "【"+(i+1) + "】" + sentence[i];
//                System.out.println(sentence[i]);
            }
            bw.write(part[0]+","+part[1]+","+ Arrays.toString(sentence).replace(",","，")+","+cfIndex+" \r\n");
        }
        bw.close();
        reader.close();
    }
    public static  String doBracket(String s){
//            if(s.contains("（") && s.contains("）")){
                int i = s.indexOf("（");
                int j = s.indexOf("）");
                s = s.substring(0,i)+"。"+ s.substring(i+1,j).replace("。",",")+"。"+s.substring(j+1);
//            }
            return s;
    }
    public static boolean fnlpTag(String s, Set<String> dic) throws LoadModelException {
        CNFactory factory = CNFactory.getInstance("resource/models");
        String[] words = factory.seg(s);
        int count = 0;
        for(String word : words){
            System.out.print(word+"  ");
            if(dic.contains(word))
                count++;
        }
        System.out.println(count);
        return count>3 ? true : false;
    }
    /*
    * 初始化词典
    * */
    public static void initialDic(Set<String> words) throws IOException {
//        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("resource/prescription.txt"),"GBK"));
//        String prescriptionName;
//        while((prescriptionName = br.readLine()) != null){
//            UserDefineLibrary.insertWord(prescriptionName, "userDefine", 1000);
//        }
        for (String word : words){
            UserDefineLibrary.insertWord(word, "userDefine", 1000);
        }
    }
    public static boolean ansjTag(String s) {
//        List<Term> parser = NlpAnalysis.parse(s).getTerms();
        //针对现代医案，直接有主方属性
        if(s.contains("主方") && !s.contains("自拟方")){
            System.out.println(s+" : true");
            return true;
        }
        //针对部分精粹医案，含有原方，另加词“减”、“加”、“去”
        if(s.contains("原方") && (s.contains("减") || s.contains("加") || s.contains("去"))){
            System.out.println(s+" : true");
            return true;
        }
        List<Term> parser = DicAnalysis.parse(s).getTerms();
        int count = 0;
        for (Term term : parser) {
            String word = term.getName();
            System.out.print(word+"  ");
            //判断是否直接采用经方
            if (dicPre.contains(word)) {
                System.out.println(word+" : true");
                return true;
            }
            //判断是否含有三个及以上的中药名
            if (dic.contains(word)) {
                count++;
            }
        }
        System.out.println(count);
        return count > 2 ? true : false;
    }
}
