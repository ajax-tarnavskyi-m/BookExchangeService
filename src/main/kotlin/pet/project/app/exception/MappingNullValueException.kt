package pet.project.app.exception

class MappingNullValueException(
    fieldName: String,
    modelName: String,
) : Exception("Cant map null value for $fieldName filed of $modelName model")
