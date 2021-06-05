package pt.isel.pdm.drag.game.model

interface Dto<out T : Model<Dto<T>>> {

    /**
     * Convert this DTO to a Model object
     * @return the model object
     * @see Model<T>
     */
    fun mapToModel(): T

}

interface Model<out T : Dto<Model<T>>> {

    /**
     * Converts this Model to a DTO object
     * @return the dto object
     * @see Dto<T>
     */
    fun mapToDto(): T

}