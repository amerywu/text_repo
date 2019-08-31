package ikoda.nlp.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import ikoda.nlp.analysis.TALog;

public class CollegeRawDataUnit
{

    public static final String COLUMN_HEAD_CATEGORY = "aa_label";
    public static final String COLUMN_HEAD_ID = "a_uid";
    public static final String COLUMN_HEAD_SOURCE = "aa_source";
    public static final String COLUMN_HEAD_WEBSITE = "aa_website";
    public static final String COLUMN_HEAD_URI = "aa_uri";
    public static final String COLUMN_HEAD_URL = "aa_url";
    public static final String COLUMN_HEAD_URL_REPOSITORY = "aa_urlrepository";
    public static final String ZERO = "0";
    public static final String LEMMATIZED_SENTENCE = "LEMMATIZED_SENTENCE";
    public static final String RAW_SENTENCE = "RAW_SENTENCE";
    

    private String website;
    private String rawData;
    private String urlRepository;
    private String sourceIdentifier;
    private String category;
    private String documentUid;
    private String uri;
    private String url;
    private Integer position=0;
    private List<IdentifiedToken> itokenList = new ArrayList();

    public CollegeRawDataUnit(long rduId)
    {

        documentUid = String.valueOf(rduId);
    }

    private void generateRandomId()
    {
        Random r = new Random();
        int i = UUID.randomUUID().hashCode();
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

    public Integer getPosition()
    {
        return position;
    }

    public String getSourceIdentifier()
    {
        return sourceIdentifier;
    }
    
    public String getUid()
    {
        
        return getDocumentUid()+ZERO+position;
    }
    
    public String getUri()
    {
        return uri;
    }
    
    

    public String getRawData() {
		return rawData;
	}

	public void setRawData(String rawData) {
		this.rawData = rawData;
	}

	public String getUrlRepository()
    {
        return urlRepository;
    }

    public void setUrlRepository(String urlRepository)
    {
        this.urlRepository = urlRepository;
    }

    public String getUrl()
    {
        return url;
    }

    public String getWebsite()
    {
        return website;
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

    public void setPosition(Integer position)
    {
        this.position = position;
    }

    public void setSourceIdentifier(String insourceIdentifier)
    {
        this.sourceIdentifier = insourceIdentifier;
    }

    public void setUri(String uri)
    {
        this.uri = uri;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public void setWebsite(String website)
    {
        this.website = website;
    }
}
