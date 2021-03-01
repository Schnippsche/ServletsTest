package de.destatis.regdb.dateiimport.job;

import de.destatis.regdb.JobBean;
import de.destatis.regdb.dateiimport.ImportFormat;
import de.destatis.regdb.dateiimport.job.adressimport.AdressImportJob;
import de.destatis.regdb.dateiimport.job.melderkontoimport.MelderkontoImportJob;
import de.destatis.regdb.dateiimport.job.registerimport.RegisterImportJob;
import de.destatis.regdb.dateiimport.job.vorbelegungsimport.VorbelegungsImportJob;
import de.destatis.regdb.dateiimport.job.xmlimport.XmlImportJob;
import de.werum.sis.idev.res.job.JobException;

public class JobFactory
{

  public static AbstractJob createJob(ImportFormat importFormat, JobBean jobBean) throws JobException
  {
    switch (importFormat)
    {
      case IMPORTOHNEZUSATZFELDER:
      case IMPORTMITZUSATZFELDER:
      case IMPORTMITANSPRECHPARTNER:
        return new AdressImportJob(jobBean);
      case REGISTERIMPORT:
        return new RegisterImportJob(jobBean);
      case VORBELEGUNGSIMPORT:
      case VORBELEGUNGDOWNLOADIMPORT:
        return new VorbelegungsImportJob(jobBean);
      case XMLIMPORT:
        return new XmlImportJob(jobBean);
      case MELDERKONTOIMPORT:
        return new MelderkontoImportJob(jobBean);
      default:
        throw new JobException("Unbekanntes Format");
    }
  }
}
