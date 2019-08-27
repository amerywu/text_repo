package ikoda.nlp.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ikoda.nlp.analysis.TALog;

public class CollegeURLRawDataUnit
{

    public static final String COLUMN_HEAD_CATEGORY = "AA_CATEGORY";
    public static final String COLUMN_HEAD_URL = "AA_URL";


    public static final String ZERO = "0";

    


    private String url;
    private String category;
    private String documentUid;
    private String target;


    private List<IdentifiedToken> itokenList = new ArrayList<>();

    public CollegeURLRawDataUnit(long rduId)
    {

        
        documentUid = String.valueOf(rduId);
    }

    private void generateRandomId()
    {
        
        Random r = new Random();
        int i = r.nextInt(1000000);
        TALog.getLogger().debug("Random DocId generated: "+i);
        documentUid=String.valueOf(i);

    }

    public String getCategory()
    {
        return category;
    }

    public String getDocumentUid()
    {
        if(null==documentUid)
        {
            generateRandomId();
        }
        return documentUid;
    }

    public List<IdentifiedToken> getItokenList()
    {
        return itokenList;
    }


    
    public String getUid()
    {
        
        return getDocumentUid();
    }
    


    public String getTarget()
    {
        return target;
    }

    public void setTarget(String target)
    {
        this.target = target;
    }

    public String getUrl()
    {
        return url;
    }


    public void setCategory(String category)
    {
        this.category = category;
    }

    public void setDocumentUid(String uid)
    {
        this.documentUid = uid;
    }

    public void setItokenList(List<IdentifiedToken> itokenList)
    {
        this.itokenList = itokenList;
    }



    public void setUrl(String url)
    {
        this.url = url;
    }


    
    

}
