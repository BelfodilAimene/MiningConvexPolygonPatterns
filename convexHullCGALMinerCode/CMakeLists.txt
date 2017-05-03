# Created by the script cgal_create_CMakeLists
# This is the CMake script for compiling a set of CGAL applications.

project( convexHullCGALMiner )


cmake_minimum_required(VERSION 2.8.11)

# CGAL and its components
find_package( CGAL QUIET COMPONENTS  )

if ( NOT CGAL_FOUND )

  message(STATUS "This project requires the CGAL library, and will not be compiled.")
  return()  

endif()

# include helper file
include( ${CGAL_USE_FILE} )


# Boost and its components
find_package( Boost REQUIRED )

if ( NOT Boost_FOUND )

  message(STATUS "This project requires the Boost library, and will not be compiled.")

  return()  

endif()

# include for local directory
# include for local package


# Creating entries for target: convexHullCGALMiner
# ############################
set(FILES ./PolytopTopDownMiner.cpp ./PolytopBottomUpMiner.cpp ./PolytopNaiveBottomUpMiner.cpp ./Utils.cpp ./Dataset.cpp ./PolytopTopDownMiner.h ./PolytopBottomUpMiner.h ./PolytopNaiveBottomUpMiner.h ./Utils.h)
add_executable( convexHullCGALMiner  main.cpp ${FILES})
#add_executable( dist/Debug/GNU-Linux/convexhullcgalminer  main.cpp ${FILES})

add_to_cached_list( CGAL_EXECUTABLE_TARGETS convexHullCGALMiner )

# Link the executable to CGAL and third-party libraries
target_link_libraries(convexHullCGALMiner   ${CGAL_LIBRARIES} ${CGAL_3RD_PARTY_LIBRARIES} )

