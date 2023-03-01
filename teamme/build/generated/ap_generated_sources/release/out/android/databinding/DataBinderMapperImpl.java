package android.databinding;

public class DataBinderMapperImpl extends MergedDataBinderMapper {
  DataBinderMapperImpl() {
    addMapper(new de.pasligh.android.teamme.DataBinderMapperImpl());
  }
}
