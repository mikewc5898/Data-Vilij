import dataprocessors.TSDProcessor;
import javafx.geometry.Point2D;
import org.junit.Assert;
import org.junit.Test;
import java.io.File;
import java.io.FileWriter;
public class JUnitTest {
//Tests for parsing one line of data in TSD  format
    @Test
    public void parseData() throws Exception {
        TSDProcessor processor = new TSDProcessor();
        String tsdLine = "@a\ta\t1,5";
        processor.processString(tsdLine);
        Assert.assertEquals("a",processor.getDataLabels().get("@a"));
        Assert.assertEquals(new Point2D(1.0,5.0),processor.getDataPoints().get("@a"));
    }
    @Test(expected = Exception.class)
    public void parseDataInvalidName()throws Exception{
        TSDProcessor processor = new TSDProcessor();
        String tsdLine = "a\ta\t0,0";
        processor.processString(tsdLine);
        Assert.assertEquals("a",processor.getDataLabels().get("@a"));
        Assert.assertEquals(new Point2D(0.0,0.0),processor.getDataPoints().get("@a"));
    }
    @Test
    public void parseDataEmptyLabel() throws Exception {
        TSDProcessor processor = new TSDProcessor();
        String tsdLine = "@a\tnull\t1,5";
        processor.processString(tsdLine);
        Assert.assertEquals("null", processor.getDataLabels().get("@a"));
        Assert.assertEquals(new Point2D(1.0, 5.0), processor.getDataPoints().get("@a"));
    }
    @Test(expected = Exception.class)
    public void parseDataInvalidPoints()throws Exception{
        TSDProcessor processor = new TSDProcessor();
        String tsdLine = "@a\ta\ta,b";
        processor.processString(tsdLine);
        Assert.assertEquals("a", processor.getDataLabels().get("@a"));
    }
    @Test(expected = Exception.class)
    public void parseDataNoTab()throws Exception{
        TSDProcessor processor = new TSDProcessor();
        String tsdLine ="@a a 1,5";
        processor.processString(tsdLine);
        Assert.assertEquals("a", processor.getDataLabels().get("@a"));
        Assert.assertEquals(new Point2D(1.0, 5.0), processor.getDataPoints().get("@a"));
    }
    //Tests for saving data in the TSD format
    @Test
    public void saveData()throws Exception{
        File file = new File("testFile.tsd");
        String data = "@a\ta\t1,5\n@b\ta\t2,4";
        saveFile(file,data);
    }
    @Test(expected=Exception.class)
    public void saveDataNotTSD()throws Exception{
        File file = new File("testFile");
        String data = "@a\ta\t1,5\n@b\ta\t2,4";
        saveFile(file,data);
    }
    @Test(expected=Exception.class)
    public void saveNoFile()throws Exception{
        File file = null;
        String data = "@a\ta\t1,5\n@b\ta\t2,4";
        saveFile(file,data);
    }
    //Test for inputs
    @Test
    //Parsing into RandomClassifier
    public void testClassifier(){
        int[] test = handleOkRequest("25","5",true,"0",0);
        Assert.assertEquals(test[0],25);
        Assert.assertEquals(test[1],5);
    }
    @Test
    public void testClassifierBoundary(){
        int[] test = handleOkRequest("1","1",true,"0",0);
        Assert.assertEquals(test[0],1);
        Assert.assertEquals(test[1],1);
    }
    @Test(expected = NumberFormatException.class)
    public void testClassifierError(){
        int[] test = handleOkRequest("a","egwrgsefw",false,"0",0);
        Assert.assertEquals(test[0],1);
        Assert.assertEquals(test[1],1);
    }


    @Test
    //Parsing into RandomClusterer
    public void testRandomClusterer(){
        int[] test = handleOkRequest("25","5",true,"3",3);
        Assert.assertEquals(test[0],25);
        Assert.assertEquals(test[1],5);
        Assert.assertEquals(test[2],3);
    }
    @Test
    public void testRandomClustererBoundary(){
        int[] test = handleOkRequest("1","1",false,"1",3);
        Assert.assertEquals(test[0],1);
        Assert.assertEquals(test[1],1);
        Assert.assertEquals(test[2],1);
    }
    @Test(expected = NumberFormatException.class)
    public void testRandomClustererError(){
        int[] test = handleOkRequest("2","5",false,"1",3);
        Assert.assertEquals(test[0],1);
        Assert.assertEquals(test[1],1);
        Assert.assertEquals(test[2],1);
    }
    @Test
    //Parsing into KMeansClusterer
    public void testKMeansClusterer(){
        int[] test = handleOkRequest("50","5",true,"2",4);
        Assert.assertEquals(test[0],50);
        Assert.assertEquals(test[1],5);
        Assert.assertEquals(test[2],2);
    }
    @Test
    public void testKMeansClustererBoundary() {
        int[] test = handleOkRequest("1", "1", false, "1", 4);
        Assert.assertEquals(test[0], 1);
        Assert.assertEquals(test[1], 1);
        Assert.assertEquals(test[2], 1);
    }
    @Test(expected = NumberFormatException.class)
    public void testKMeansClustererError(){
        int[] test = handleOkRequest("5","0",true,"2",4);
        Assert.assertEquals(test[0],1);
        Assert.assertEquals(test[1],1);
        Assert.assertEquals(test[2],1);
    }












    private void saveFile(File saveFile,String tsd)throws Exception{
        String name = saveFile.getName();
        int i = name.lastIndexOf('.');
        String extension = name.substring(i);
        if(!extension.equals(".tsd")){
            throw new Exception();
        }
        TSDProcessor processor = new TSDProcessor();
        processor.processString(tsd);
        FileWriter dataWriter = new FileWriter(saveFile);
        dataWriter.write(processor.labelInstance());
        dataWriter.close();
    }

    private int[] handleOkRequest(String iteration, String interval, Boolean continuous,String numClusters, int option) throws NumberFormatException{
        int[] parsedData = new int[]{0,0};
        int[] parsedCluster = new int[]{0,0,0};
        int[] result = null;
        switch(option){
            case 0:
                    if (Integer.parseInt(iteration) > 0) {
                        parsedData[0] = Integer.parseInt(iteration);
                    }
                    else throw new NumberFormatException();
                    if (Integer.parseInt(interval) > 0 && Integer.parseInt(interval) <= Integer.parseInt(iteration)) {
                       parsedData[1] = Integer.parseInt(interval);
                    }
                    else throw new NumberFormatException();
                    result = parsedData;
                break;

            case 3:
                    if (Integer.parseInt(iteration) > 0) {
                        parsedCluster[0] = Integer.parseInt(iteration);
                    }
                    else throw new NumberFormatException();
                    if (Integer.parseInt(interval) > 0 && Integer.parseInt(interval) <= Integer.parseInt(iteration)) {
                       parsedCluster[1] = Integer.parseInt(interval);
                    }
                    else throw new NumberFormatException();
                    if(Integer.parseInt(numClusters)>0) {
                        parsedCluster[2] = Integer.parseInt(numClusters);
                    }
                    else throw new NumberFormatException();
                    result = parsedCluster;
                break;
            case 4:
                    if (Integer.parseInt(iteration) > 0) {
                        parsedCluster[0] = Integer.parseInt(iteration);
                    }
                    else throw new NumberFormatException();
                    if (Integer.parseInt(interval) > 0 && Integer.parseInt(interval) <= Integer.parseInt(iteration)) {
                        parsedCluster[1] = Integer.parseInt(interval);
                    }
                    else throw new NumberFormatException();
                    if(Integer.parseInt(numClusters)>0) {
                        parsedCluster[2] = Integer.parseInt(numClusters);
                    }
                    else throw new NumberFormatException();
                    result = parsedCluster;
                   break;

        }
        return result;
    }
}