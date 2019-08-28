package ikoda.nlp.structure;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

public class EsJson {
	
	public final static String JOBS_INDEX_NAME = "job-descriptions-index";
	public final static String POSSIBLE_PHRASES_INDEX_NAME = "possible-phrases-index";

	public static XContentBuilder jobsIndexJson() throws Exception {
		XContentBuilder builder = XContentFactory.jsonBuilder();
		builder.startObject();
		{
			builder.startObject("properties");
			{

				builder.startObject("location");
				{
					builder.field("type", "keyword");
				}
				builder.endObject();
				builder.startObject("salaryStart");
				{
					builder.field("type", "text");
				}
				builder.endObject();
				builder.startObject("salaryEnd");
				{
					builder.field("type", "text");
				}
				builder.endObject();
				builder.startObject("jobTitles");
				{
					builder.field("type", "text");
				}
				builder.endObject();
				builder.startObject("qualifications");
				{
					builder.field("type", "keyword");
				}
				builder.endObject();
				builder.startObject("areasOfStudy");
				{
					builder.field("type", "keyword");
				}
				builder.endObject();
				builder.startObject("certification");
				{
					builder.field("type", "keyword");
				}
				builder.endObject();
				builder.startObject("skills");
				{
					builder.field("type", "text");
				}
				builder.endObject();
				builder.startObject("relatedMajors");
				{
					builder.field("type", "text");
				}
				builder.endObject();
				builder.startObject("workSkills");
				{
					builder.field("type", "text");
				}
				builder.endObject();
				builder.startObject("region");
				{
					builder.field("type", "text");
				}
				builder.endObject();
				builder.startObject("contentType");
				{
					builder.field("type", "keyword");
				}
				builder.endObject();
				builder.startObject("degreeLevel");
				{
					builder.field("type", "keyword");
				}
				builder.endObject();
				builder.startObject("detailLevel");
				{
					builder.field("type", "text");
				}
				builder.endObject();
				builder.startObject("yearsExperienceAsInt");
				{
					builder.field("type", "text");
				}
				builder.endObject();
				builder.startObject("majorFinal");
				{
					builder.field("type", "keyword");
				}
				builder.endObject();
				builder.startObject("jobFinal");
				{
					builder.field("type", "text");
				}
				builder.endObject();
				builder.startObject("databaseDescriptor");
				{
					builder.field("type", "text");
				}
				builder.endObject();
				builder.startObject("created");
				{
					builder.field("type", "date");
				}
				builder.endObject();
			}
			builder.endObject();
		}
		builder.endObject();
		return builder;
	}
	
	
	public static XContentBuilder possiblePhrasesIndexJson() throws Exception {
		XContentBuilder builder = XContentFactory.jsonBuilder();
		builder.startObject();
		{
			builder.startObject("properties");
			{
				builder.startObject("possible-phrase");
				{
					builder.field("type", "text");
				}
				builder.endObject();
	
				builder.startObject("token-type");
				{
					builder.field("type", "text");
				}
				builder.endObject();
			}
			builder.endObject();
		}
		builder.endObject();
		return builder;
	}
}
